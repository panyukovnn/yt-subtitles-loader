package ru.panyukovnn.ytsubtitlesloader.service;

import ru.panyukovnn.ytsubtitlesloader.dto.YtSubtitles;

public interface YtSubtitlesLoader {

    YtSubtitles load(String dirtyLink);
}
