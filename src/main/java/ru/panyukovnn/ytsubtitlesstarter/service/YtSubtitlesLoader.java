package ru.panyukovnn.ytsubtitlesstarter.service;

import ru.panyukovnn.ytsubtitlesstarter.dto.YtSubtitles;

public interface YtSubtitlesLoader {

    YtSubtitles load(String dirtyLink);
}
