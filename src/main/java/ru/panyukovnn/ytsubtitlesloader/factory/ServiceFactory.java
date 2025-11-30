package ru.panyukovnn.ytsubtitlesloader.factory;

import ru.panyukovnn.ytsubtitlesloader.service.YtDlpProcessBuilderCreator;
import ru.panyukovnn.ytsubtitlesloader.service.YtSubtitlesLoader;
import ru.panyukovnn.ytsubtitlesloader.service.YtSubtitlesLoaderImpl;
import ru.panyukovnn.ytsubtitlesloader.util.YtDlpExecutableExtractor;
import ru.panyukovnn.ytsubtitlesloader.util.YtLinkHelper;

import java.nio.file.Path;

/**
 * Фабрика для создания сервисов приложения.
 * Заменяет Spring DI на ручную инициализацию зависимостей.
 */
public class ServiceFactory {

    /**
     * Создает и возвращает полностью инициализированный YtSubtitlesLoader
     */
    public YtSubtitlesLoader createYtSubtitlesLoader() {
        Path ytDlpPath = YtDlpExecutableExtractor.extractExecutable();
        YtDlpProcessBuilderCreator processBuilderCreator = new YtDlpProcessBuilderCreator(ytDlpPath);
        YtLinkHelper ytLinkHelper = new YtLinkHelper();
        return new YtSubtitlesLoaderImpl(ytLinkHelper, processBuilderCreator);
    }
}
