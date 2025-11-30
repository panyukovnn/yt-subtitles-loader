package ru.panyukovnn.ytsubtitlesloader.util;

import ru.panyukovnn.ytsubtitlesloader.exception.YtLoadingException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

/**
 * Утилита для извлечения исполняемого файла yt-dlp из ресурсов JAR.
 * Подобная инициализация необходима, поскольку файл находится в ресурсах и не может быть исполнен сам по себе.
 */
public class YtDlpExecutableExtractor {

    public static Path extractExecutable() {
        try {
            String executableFileName = determineExecutableFileName();
            String resourcePath = "/yt-dlp/" + executableFileName;

            InputStream resourceStream = YtDlpExecutableExtractor.class.getResourceAsStream(resourcePath);
            if (resourceStream == null) {
                throw new YtLoadingException("4825", "Не удалось найти исполняемый файл yt-dlp в ресурсах: " + resourcePath);
            }

            Path tempFile = Files.createTempFile("yt-dlp_", "_" + executableFileName);
            tempFile.toFile().deleteOnExit();

            Files.copy(resourceStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            resourceStream.close();

            setExecutablePermissions(tempFile);

            return tempFile;
        } catch (IOException e) {
            throw new YtLoadingException("4826", "Ошибка при извлечении исполняемого файла yt-dlp: " + e.getMessage());
        }
    }

    private static void setExecutablePermissions(Path tempFile) throws IOException {
        try {
            Set<PosixFilePermission> permissions = new HashSet<>();
            permissions.add(PosixFilePermission.OWNER_READ);
            permissions.add(PosixFilePermission.OWNER_WRITE);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
            Files.setPosixFilePermissions(tempFile, permissions);
        } catch (UnsupportedOperationException | IOException e) {
            tempFile.toFile().setExecutable(true, false);
        }
    }

    private static String determineExecutableFileName() {
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
