package ru.panyukovnn.ytsubtitlesstarter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoadSubtitlesRequest {

    @NotBlank(message = "Ссылка на YouTube видео не может быть пустой")
    @Pattern(
        regexp = "^https?://(www\\.)?(youtube\\.com|youtu\\.be)/.*$",
        message = "Некорректный формат YouTube ссылки"
    )
    private String youtubeLink;
}
