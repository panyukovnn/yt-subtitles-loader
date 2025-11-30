package ru.panyukovnn.ytsubtitlesloader.service;

import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.panyukovnn.ytsubtitlesloader.dto.SubtitlesLang;
import ru.panyukovnn.ytsubtitlesloader.dto.YtSubtitles;
import ru.panyukovnn.ytsubtitlesloader.exception.YtLoadingException;
import ru.panyukovnn.ytsubtitlesloader.util.YtLinkHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class YtSubtitlesLoaderImpl implements YtSubtitlesLoader {

    private static final Logger log = LoggerFactory.getLogger(YtSubtitlesLoaderImpl.class);

    private final YtLinkHelper ytLinkHelper;
    private final YtDlpProcessBuilderCreator ytDlpProcessBuilderCreator;

    public YtSubtitlesLoaderImpl(YtLinkHelper ytLinkHelper, YtDlpProcessBuilderCreator ytDlpProcessBuilderCreator) {
        this.ytLinkHelper = ytLinkHelper;
        this.ytDlpProcessBuilderCreator = ytDlpProcessBuilderCreator;
    }

    public YtSubtitles load(String dirtyLink) {
        log.debug("Начинаю загрузку субтитров из youtube видео по ссылке: {}", dirtyLink);

        // Очистка старых временных файлов
        cleanupOldTempFiles();

        if (!ytLinkHelper.isValidYoutubeUrl(dirtyLink)) {
            throw new YtLoadingException("824c", "Невалидная ссылка youtube: " + dirtyLink);
        }

        String cleanedLink = ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(dirtyLink);

        try {
            Pair<SubtitlesLang, Boolean> preferred = selectPreferredSubtitles(cleanedLink)
                .orElseThrow(() -> new YtLoadingException("48ae", "Не удалось найти подходящие субтитры (vtt) для указанного видео"));

            SubtitlesLang lang = preferred.getLeft();
            boolean isAuto = preferred.getRight();

            String subtitlesContent = tryDownloadSubtitles(cleanedLink, lang, isAuto);
            if (StringUtils.isEmpty(subtitlesContent)) {
                throw new YtLoadingException("48ae", "Не удалось загрузить субтитры для указанного видео");
            }

            List<String> subtitlesLines = List.of(subtitlesContent.split("\n"));

            List<String> cleanedFileLines = cleanSubtitles(subtitlesLines);

            String subtitles = String.join(" ", cleanedFileLines);

            return new YtSubtitles(cleanedLink, null, lang, subtitles);
        } catch (YtLoadingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка загрузки субтитров из видео: {}", e.getMessage(), e);

            throw new YtLoadingException("63e9", "Не удалось извлечь субтитры из видео", e);
        }
    }

    /**
     * Сначала вызываем yt-dlp --list-subs, парсим доступные субтитры и выбираем лучший вариант
     * по приоритету: ru manual -> ru auto -> en manual -> en auto. Берем только если есть формат vtt.
     */
    private Optional<Pair<SubtitlesLang, Boolean>> selectPreferredSubtitles(String videoUrl) {
        ProcessBuilder processBuilder = ytDlpProcessBuilderCreator.createListSubsProcessBuilder(videoUrl);

        try {
            log.debug("Получение списка доступных субтитров для видео: {}", videoUrl);

            Process process = processBuilder.start();

            BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            boolean inAutomaticSection = false;

            boolean ruManualVtt = false;
            boolean ruAutoVtt = false;
            boolean enManualVtt = false;
            boolean enAutoVtt = false;

            while ((line = stdoutReader.readLine()) != null) {
                String lower = line.toLowerCase();

                if (lower.contains("automatic captions")) {
                    inAutomaticSection = true;
                    continue;
                }
                if (lower.contains("available subtitles")) {
                    inAutomaticSection = false;
                    continue;
                }

                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }

                // Ожидаем строки вида: "<langCode>    <formats...>"
                int firstSpace = trimmed.indexOf(' ');
                if (firstSpace <= 0) {
                    continue;
                }

                String langCode = trimmed.substring(0, firstSpace).trim();
                String formats = trimmed.substring(firstSpace).toLowerCase();

                // Пропускаем заголовки "Language  formats" и т.п.
                if ("language".equals(langCode)) {
                    continue;
                }

                boolean hasVtt = formats.contains("vtt");
                if (!hasVtt) {
                    continue;
                }

                // Считаем en/ru как базовые коды и их варианты (en-GB, ru-...).
                boolean isRu = "ru".equals(langCode) || langCode.startsWith("ru-");
                boolean isEn = "en".equals(langCode) || langCode.startsWith("en-");

                if (isRu) {
                    if (inAutomaticSection) {
                        ruAutoVtt = true;
                    } else {
                        ruManualVtt = true;
                    }
                } else if (isEn) {
                    if (inAutomaticSection) {
                        enAutoVtt = true;
                    } else {
                        enManualVtt = true;
                    }
                }
            }

            // Собираем stderr на случай ошибок
            StringBuilder errorOutput = new StringBuilder();
            while ((line = stderrReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                if (StringUtils.isNotBlank(errorOutput)) {
                    log.warn("yt-dlp --list-subs stderr: {}", errorOutput);
                }

                log.warn("a3d1 Ошибка получения списка субтитров, exitCode: {}", exitCode);
                return Optional.empty();
            }

            // Приоритет: ru manual -> ru auto -> en manual -> en auto
            if (ruManualVtt) {
                return Optional.of(Pair.of(SubtitlesLang.RU, false));
            }
            if (ruAutoVtt) {
                return Optional.of(Pair.of(SubtitlesLang.RU, true));
            }
            if (enManualVtt) {
                return Optional.of(Pair.of(SubtitlesLang.EN, false));
            }
            if (enAutoVtt) {
                return Optional.of(Pair.of(SubtitlesLang.EN, true));
            }

            return Optional.empty();
        } catch (Exception e) {
            log.error("7b52 Ошибка при получении списка субтитров: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * @param videoUrl   ссылка на видео на youtube
     * @param lang       язык субтитров (ru или en)
     * @param isAutoSubs загружать ли автогенерированные субтитры
     * @return содержимое субтитров из файла, созданного yt-dlp
     */
    @Nullable
    private String tryDownloadSubtitles(String videoUrl, SubtitlesLang lang, boolean isAutoSubs) {
        try {
            log.debug("Начало загрузки субтитров для видео: {}", videoUrl);

            Pair<ProcessBuilder, Path> builderAndPath = ytDlpProcessBuilderCreator.createProcessBuilder(
                videoUrl, lang.getLang(), isAutoSubs
            );
            ProcessBuilder processBuilder = builderAndPath.getLeft();
            Path outputPath = builderAndPath.getRight();

            Process process = processBuilder.start();

            // Читаем stderr для отслеживания ошибок
            BufferedReader stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder errorOutput = new StringBuilder();
            String line;

            while ((line = stderrReader.readLine()) != null) {
                errorOutput.append(line).append("\n");

                if (line.contains("There are no subtitles for the requested languages")) {
                    log.warn("There are no subtitles for the requested languages");
                    return null;
                }
            }

            if (StringUtils.isNotBlank(errorOutput)) {
                log.warn("yt-dlp stderr: {}", errorOutput);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.warn("12d7 Ошибка выгрузки субтитров с помощью yt-dlp, exitCode: {}", exitCode);

                if (!errorOutput.isEmpty()) {
                    log.warn("Error output: {}", errorOutput);
                }

                return null;
            }

            // Читаем содержимое файла субтитров
            if (!Files.exists(outputPath)) {
                log.warn("Файл субтитров не был создан: {}", outputPath);
                return null;
            }

            String subtitles = Files.readString(outputPath).trim();

            // Удаляем временный файл
            try {
                Files.delete(outputPath);
            } catch (Exception e) {
                log.warn("Не удалось удалить временный файл: {}", outputPath, e);
            }

            if (subtitles.isEmpty()) {
                log.warn("Субтитры пусты для видео: {}", videoUrl);
                return null;
            }

            log.debug("Субтитры успешно загружены: {}", videoUrl);

            return subtitles;
        } catch (Exception e) {
            log.error("45bb Ошибка выгрузки субтитров с помощью yt-dlp: {}", e.getMessage(), e);

            return null;
        }
    }

    private List<String> cleanSubtitles(List<String> lines) {
        List<String> cleanedLines = new ArrayList<>();

        for (String line : lines) {
            // Убираем теги и метки
            String cleanedLine = line
                .replaceAll("<[^>]+>", "")                         // Удаляет все теги вида <...>
                .replaceAll("\\d{2}:\\d{2}:\\d{2}\\.\\d{3}", "")   // Удаляет временные метки
                .replaceAll("-->.*", "")                           // Удаляет строки с временными интервалами
                .replaceAll("align:\\w+ position:\\d+%", "")       // Удаляет служебные параметры
                .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "")         // Удаляет управляющие символы
                .replaceAll("\\s{2,}", " ")                        // Заменяет множественные пробелы на один
                .trim();

            if (!cleanedLine.isEmpty()) {
                cleanedLines.add(cleanedLine);
            }
        }

        // Пропускаем первую 3 строчки с метаинформацией
        if (lines.size() > 3) {
            if (lines.getFirst().startsWith("WEBVTT")
                && lines.get(1).startsWith("Kind: ")
                && lines.get(2).startsWith("Language: ")) {
                return cleanedLines.subList(3, cleanedLines.size() - 1);
            }
        }

        return cleanedLines;
    }

    /**
     * Удаляет временные файлы субтитров, которые были созданы более 15 минут назад.
     * Это помогает очистить файлы, которые не были удалены из-за неожиданного завершения программы.
     */
    private void cleanupOldTempFiles() {
        Path tempDir = Path.of("./temp-subtitles");

        if (!Files.exists(tempDir)) {
            return;
        }

        try (Stream<Path> files = Files.list(tempDir)) {
            Instant fifteenMinutesAgo = Instant.now().minus(15, ChronoUnit.MINUTES);

            files.forEach(file -> {
                try {
                    BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
                    Instant creationTime = attrs.creationTime().toInstant();

                    if (creationTime.isBefore(fifteenMinutesAgo)) {
                        Files.delete(file);
                        log.debug("Удален старый временный файл: {}", file);
                    }
                } catch (IOException e) {
                    log.warn("Не удалось проверить или удалить временный файл: {}", file, e);
                }
            });
        } catch (IOException e) {
            log.warn("Ошибка при очистке временных файлов: {}", e.getMessage(), e);
        }
    }
}
