package ru.panyukovnn.ytsubtitlesstarter.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Данный класс вынесен отдельно для удобства тестирования
 */
@Service
@RequiredArgsConstructor
public class YtDlpProcessBuilderCreator {

    private final Path ytDlpExecutablePath;

    public ProcessBuilder createListSubsProcessBuilder(String videoUrl) {
        return new ProcessBuilder(
            ytDlpExecutablePath.toString(),
            "--list-subs",
            videoUrl
        ).directory(new File("."));
    }

    public Pair<ProcessBuilder, Path> createProcessBuilder(String videoUrl, String lang, boolean isAutoSubs) throws IOException {
        // Создаём директорию для временных файлов
        Path tempDir = Path.of("./temp-subtitles");
        Files.createDirectories(tempDir);

        // Указываем имя без расширения, т.к. yt-dlp автоматически добавит язык и .vtt
        String tempFileNameWithoutExt = "temp_subs_" + UUID.randomUUID().toString().substring(0, 8);
        Path outputPathTemplate = tempDir.resolve(tempFileNameWithoutExt);
        String subsType = isAutoSubs ? "--write-auto-subs" : "--write-subs";

        ProcessBuilder processBuilder = new ProcessBuilder(
            ytDlpExecutablePath.toString(),
            "--skip-download",
            subsType,
            "--sub-lang", lang,
            "--sub-format", "vtt",
            "-o", outputPathTemplate.toString(),
            videoUrl
        ).directory(new File("."));

        // Реальный файл будет иметь формат: temp_subs_<timestamp>.<lang>.vtt
        Path actualOutputPath = tempDir.resolve(tempFileNameWithoutExt + "." + lang + ".vtt");

        return Pair.of(processBuilder, actualOutputPath);
    }
}
