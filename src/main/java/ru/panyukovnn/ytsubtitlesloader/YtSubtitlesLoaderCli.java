package ru.panyukovnn.ytsubtitlesloader;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import ru.panyukovnn.ytsubtitlesloader.dto.YtSubtitles;
import ru.panyukovnn.ytsubtitlesloader.exception.YtLoadingException;
import ru.panyukovnn.ytsubtitlesloader.factory.YtSubtitlesLoaderFactory;
import ru.panyukovnn.ytsubtitlesloader.service.YtSubtitlesLoader;

import java.util.concurrent.Callable;

/**
 * CLI приложение для загрузки и очистки субтитров из YouTube видео.
 * Использует Picocli для обработки аргументов командной строки.
 */
@Command(
    name = "yt-subtitles-loader",
    description = "Загружает и очищает субтитры из YouTube видео",
    mixinStandardHelpOptions = true,
    version = "1.0"
)
public class YtSubtitlesLoaderCli implements Callable<Integer> {

    @Parameters(
        index = "0",
        description = "YouTube video URL (e.g., https://www.youtube.com/watch?v=dQw4w9WgXcQ)"
    )
    private String youtubeUrl;

    @Override
    public Integer call() {
        try {
            YtSubtitlesLoaderFactory factory = new YtSubtitlesLoaderFactory();
            YtSubtitlesLoader loader = factory.createYtSubtitlesLoader();

            YtSubtitles result = loader.load(youtubeUrl);

            // Выводим ТОЛЬКО текст субтитров в stdout
            System.out.println(result.subtitles());

            return 0;
        } catch (YtLoadingException e) {
            // Известная ошибка загрузки - выводим в stderr с кодом ошибки
            System.err.println("Ошибка загрузки субтитров [" + e.getId() + "]: " + e.getMessage());
            return 1;
        } catch (Exception e) {
            // Неожиданная ошибка - выводим в stderr
            System.err.println("Неожиданная ошибка: " + e.getMessage());
            e.printStackTrace(System.err);
            return 2;
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new YtSubtitlesLoaderCli()).execute(args);
        System.exit(exitCode);
    }
}
