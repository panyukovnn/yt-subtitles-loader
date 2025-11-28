package ru.panyukovnn.ytsubtitlesstarter.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.panyukovnn.ytsubtitlesstarter.dto.LoadSubtitlesRequest;
import ru.panyukovnn.ytsubtitlesstarter.dto.LoadSubtitlesResponse;
import ru.panyukovnn.ytsubtitlesstarter.dto.YtSubtitles;
import ru.panyukovnn.ytsubtitlesstarter.service.YtSubtitlesLoader;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class YtSubtitlesController {

    private final YtSubtitlesLoader ytSubtitlesLoader;

    @PostMapping("/load-subtitles")
    public LoadSubtitlesResponse loadSubtitles(@Valid @RequestBody LoadSubtitlesRequest request) {
        log.info("Получен запрос на загрузку субтитров для видео: {}", request.getYoutubeLink());

        YtSubtitles ytSubtitles = ytSubtitlesLoader.load(request.getYoutubeLink());

        return LoadSubtitlesResponse.builder()
            .link(ytSubtitles.link())
            .title(ytSubtitles.title())
            .lang(ytSubtitles.lang())
            .subtitles(ytSubtitles.subtitles())
            .build();
    }
}
