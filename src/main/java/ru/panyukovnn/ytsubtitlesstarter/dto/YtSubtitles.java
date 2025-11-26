package ru.panyukovnn.ytsubtitlesstarter.dto;

public record YtSubtitles(
    String link,
    String title,
    SubtitlesLang lang,
    String subtitles
) {
}
