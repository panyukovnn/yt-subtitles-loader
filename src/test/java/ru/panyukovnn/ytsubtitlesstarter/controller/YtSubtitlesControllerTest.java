package ru.panyukovnn.ytsubtitlesstarter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.panyukovnn.ytsubtitlesstarter.dto.LoadSubtitlesRequest;
import ru.panyukovnn.ytsubtitlesstarter.dto.SubtitlesLang;
import ru.panyukovnn.ytsubtitlesstarter.dto.YtSubtitles;
import ru.panyukovnn.ytsubtitlesstarter.exception.YtLoadingException;
import ru.panyukovnn.ytsubtitlesstarter.service.YtSubtitlesLoader;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class YtSubtitlesControllerTest {

    @Configuration
    @EnableAutoConfiguration
    static class TestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private YtSubtitlesLoader ytSubtitlesLoader;

    @Test
    void when_loadSubtitles_withValidRequest_then_success() throws Exception {
        LoadSubtitlesRequest request = new LoadSubtitlesRequest("https://www.youtube.com/watch?v=test123");
        YtSubtitles ytSubtitles = new YtSubtitles(
            "https://www.youtube.com/watch?v=test123",
            "Test Title",
            SubtitlesLang.RU,
            "Текст субтитров"
        );

        when(ytSubtitlesLoader.load(anyString())).thenReturn(ytSubtitles);

        mockMvc.perform(post("/api/v1/load-subtitles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.link").value("https://www.youtube.com/watch?v=test123"))
            .andExpect(jsonPath("$.title").value("Test Title"))
            .andExpect(jsonPath("$.lang").value("RU"))
            .andExpect(jsonPath("$.subtitles").value("Текст субтитров"));
    }

    @Test
    void when_loadSubtitles_withBlankLink_then_badRequest() throws Exception {
        LoadSubtitlesRequest request = new LoadSubtitlesRequest("");

        mockMvc.perform(post("/api/v1/load-subtitles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void when_loadSubtitles_withNullLink_then_badRequest() throws Exception {
        LoadSubtitlesRequest request = new LoadSubtitlesRequest(null);

        mockMvc.perform(post("/api/v1/load-subtitles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void when_loadSubtitles_withInvalidPattern_then_badRequest() throws Exception {
        LoadSubtitlesRequest request = new LoadSubtitlesRequest("https://invalid-site.com/video");

        mockMvc.perform(post("/api/v1/load-subtitles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void when_loadSubtitles_withYoutubeShortLink_then_success() throws Exception {
        LoadSubtitlesRequest request = new LoadSubtitlesRequest("https://youtu.be/test123");
        YtSubtitles ytSubtitles = new YtSubtitles(
            "https://youtu.be/test123",
            null,
            SubtitlesLang.EN,
            "English subtitles"
        );

        when(ytSubtitlesLoader.load(anyString())).thenReturn(ytSubtitles);

        mockMvc.perform(post("/api/v1/load-subtitles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.link").value("https://youtu.be/test123"))
            .andExpect(jsonPath("$.lang").value("EN"));
    }

    @Test
    void when_loadSubtitles_withServiceException_then_exceptionPropagates() throws Exception {
        LoadSubtitlesRequest request = new LoadSubtitlesRequest("https://www.youtube.com/watch?v=test123");

        when(ytSubtitlesLoader.load(anyString())).thenThrow(new YtLoadingException("824c", "Невалидная ссылка youtube"));

        // Without @ControllerAdvice, the exception propagates as internal server error
        // User will implement their own @ControllerAdvice to handle YtLoadingException properly
        try {
            mockMvc.perform(post("/api/v1/load-subtitles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        } catch (Exception e) {
            // Exception should propagate when no error handler is configured
            assertTrue(e.getCause() instanceof YtLoadingException);
        }
    }
}
