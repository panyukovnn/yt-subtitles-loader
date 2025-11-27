package ru.panyukovnn.ytsubtitlesstarter.util;

import org.apache.commons.lang3.tuple.Pair;
import ru.panyukovnn.ytsubtitlesstarter.exception.YtLoadingException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Данный класс вынесен отдельно для удобства тестирования
 */
public class YtDlpProcessBuilderCreator {

    private static volatile Path ytDlpExecutablePath;

    static {
        try {
            String executableFileName = defineYtDlpExecutableFileName();
            String resourcePath = "/yt-dlp/" + executableFileName;

            // Извлекаем исполняемый файл из ресурсов во временную директорию
            InputStream resourceStream = YtDlpProcessBuilderCreator.class.getResourceAsStream(resourcePath);

            if (resourceStream == null) {
                throw new YtLoadingException("4825", "Не удалось найти исполняемый файл yt-dlp в ресурсах: " + resourcePath);
            }

            // Создаем временный файл
            Path tempFile = Files.createTempFile("yt-dlp_", "_" + executableFileName);
            tempFile.toFile().deleteOnExit();

            // Копируем содержимое из ресурсов во временный файл
            Files.copy(resourceStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            resourceStream.close();

            // Устанавливаем права на выполнение (для Unix-подобных систем)
            try {
                Set<PosixFilePermission> permissions = new HashSet<>();
                permissions.add(PosixFilePermission.OWNER_READ);
                permissions.add(PosixFilePermission.OWNER_WRITE);
                permissions.add(PosixFilePermission.OWNER_EXECUTE);

                Files.setPosixFilePermissions(tempFile, permissions);
            } catch (UnsupportedOperationException | IOException e) {
                // На Windows PosixFilePermissions не поддерживаются
                tempFile.toFile().setExecutable(true, false);
            }

            ytDlpExecutablePath = tempFile;
        } catch (IOException e) {
            throw new YtLoadingException("4826", "Ошибка при извлечении исполняемого файла yt-dlp: " + e.getMessage());
        }
    }

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

    private static String defineYtDlpExecutableFileName() {
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("mac")) {
            return "yt-dlp_macos";
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            String arch = System.getProperty("os.arch").toLowerCase();

            return arch.contains("aarch64") || arch.contains("arm64")
                ? "yt-dlp_linux_aarch64"
                : "yt-dlp_linux";
        }

        throw new YtLoadingException("4824", "Не удалось определить подходящий yt-dlp исполняемый файл для системы: " + osName);
    }
}
