package ru.panyukovnn.ytsubtitlesloader.dto;

public enum SubtitlesLang {

    RU("ru"),
    EN("en");

    private final String lang;

    SubtitlesLang(String lang) {
        this.lang = lang;
    }

    public String getLang() {
        return lang;
    }
}
