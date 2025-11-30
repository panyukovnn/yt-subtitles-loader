package ru.panyukovnn.ytsubtitlesloader.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.panyukovnn.ytsubtitlesloader.exception.YtLoadingException;
import ru.panyukovnn.ytsubtitlesloader.factory.ServiceFactory;
import ru.panyukovnn.ytsubtitlesloader.service.YtSubtitlesLoader;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Интеграционные тесты для проверки работы YtSubtitlesLoader с реальными YouTube видео.
 * Тесты отключены по умолчанию (@Disabled), так как они зависят от внешнего сервиса.
 */
class YtSubtitlesLoaderIntegrationTest {

    private YtSubtitlesLoader ytSubtitlesLoader;

    @BeforeEach
    void setUp() {
        ServiceFactory factory = new ServiceFactory();
        ytSubtitlesLoader = factory.createYtSubtitlesLoader();
    }

    @Test
    void shouldThrowExceptionForInvalidUrl() {
        String invalidUrl = "https://invalid-url.com/watch?v=test";

        YtLoadingException exception = assertThrows(
            YtLoadingException.class,
            () -> ytSubtitlesLoader.load(invalidUrl)
        );

        assertThat(exception.getId(), equalTo("824c"));
        assertThat(exception.getMessage(), containsString("Невалидная ссылка youtube"));
    }
}
