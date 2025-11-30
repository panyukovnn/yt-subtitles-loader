package ru.panyukovnn.ytsubtitlesloader.dto;

public record YtSubtitles(
    String link,
    String title,
    SubtitlesLang lang,
    String subtitles
) {
}
