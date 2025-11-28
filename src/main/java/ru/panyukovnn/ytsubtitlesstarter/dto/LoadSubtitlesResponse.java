package ru.panyukovnn.ytsubtitlesstarter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoadSubtitlesResponse {

    private String link;
    private String title;
    private SubtitlesLang lang;
    private String subtitles;
}
