package ru.panyukovnn.ytsubtitlesstarter.service;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.panyukovnn.ytsubtitlesstarter.dto.SubtitlesLang;
import ru.panyukovnn.ytsubtitlesstarter.dto.YtSubtitles;
import ru.panyukovnn.ytsubtitlesstarter.exception.YtLoadingException;
import ru.panyukovnn.ytsubtitlesstarter.util.YtLinkHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class YtSubtitlesLoaderImplUnitTest {

    @Mock
    private YtLinkHelper ytLinkHelper;

    @Mock
    private YtDlpProcessBuilderCreator ytDlpProcessBuilderCreator;

    @InjectMocks
    private YtSubtitlesLoaderImpl ytSubtitlesLoader;

    @Nested
    class Load {

        @Test
        void when_load_withValidUrlAndRussianManualSubtitles_then_success(@TempDir Path tempDir) throws Exception {
            String dirtyLink = "https://www.youtube.com/watch?v=dQw4w9WgXcQ&feature=share";
            String cleanedLink = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
            String subtitlesContent = "WEBVTT\n\n00:00:01.000 --> 00:00:03.000\nПривет мир\n\n00:00:04.000 --> 00:00:06.000\nКак дела";

            Path subtitlesFile = tempDir.resolve("temp_subs_123.ru.vtt");
            Files.writeString(subtitlesFile, subtitlesContent);

            ProcessBuilder listSubsProcessBuilder = createMockProcessBuilder(createListSubsOutput(true, false, false, false), "", 0);
            ProcessBuilder downloadProcessBuilder = createMockProcessBuilder("", "", 0);

            when(ytLinkHelper.isValidYoutubeUrl(dirtyLink)).thenReturn(true);
            when(ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(dirtyLink)).thenReturn(cleanedLink);
            when(ytDlpProcessBuilderCreator.createListSubsProcessBuilder(cleanedLink)).thenReturn(listSubsProcessBuilder);
            when(ytDlpProcessBuilderCreator.createProcessBuilder(cleanedLink, "ru", false)).thenReturn(Pair.of(downloadProcessBuilder, subtitlesFile));

            YtSubtitles result = ytSubtitlesLoader.load(dirtyLink);

            assertThat(result, is(notNullValue()));
            assertThat(result.link(), is(equalTo(cleanedLink)));
            assertThat(result.title(), is(nullValue()));
            assertThat(result.lang(), is(equalTo(SubtitlesLang.RU)));
            assertThat(result.subtitles(), containsString("Привет мир"));
            assertThat(result.subtitles(), containsString("Как дела"));
            verify(ytLinkHelper).isValidYoutubeUrl(dirtyLink);
            verify(ytLinkHelper).removeRedundantQueryParamsFromYoutubeLint(dirtyLink);
            verify(ytDlpProcessBuilderCreator).createListSubsProcessBuilder(cleanedLink);
            verify(ytDlpProcessBuilderCreator).createProcessBuilder(cleanedLink, "ru", false);
        }

        @Test
        void when_load_withValidUrlAndRussianAutoSubtitles_then_success(@TempDir Path tempDir) throws Exception {
            String dirtyLink = "https://www.youtube.com/watch?v=test123";
            String cleanedLink = "https://www.youtube.com/watch?v=test123";
            String subtitlesContent = "WEBVTT\n\n00:00:01.000 --> 00:00:03.000\nАвтосубтитры";

            Path subtitlesFile = tempDir.resolve("temp_subs_456.ru.vtt");
            Files.writeString(subtitlesFile, subtitlesContent);

            ProcessBuilder listSubsProcessBuilder = createMockProcessBuilder(createListSubsOutput(false, true, false, false), "", 0);
            ProcessBuilder downloadProcessBuilder = createMockProcessBuilder("", "", 0);

            when(ytLinkHelper.isValidYoutubeUrl(dirtyLink)).thenReturn(true);
            when(ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(dirtyLink)).thenReturn(cleanedLink);
            when(ytDlpProcessBuilderCreator.createListSubsProcessBuilder(cleanedLink)).thenReturn(listSubsProcessBuilder);
            when(ytDlpProcessBuilderCreator.createProcessBuilder(cleanedLink, "ru", true)).thenReturn(Pair.of(downloadProcessBuilder, subtitlesFile));

            YtSubtitles result = ytSubtitlesLoader.load(dirtyLink);

            assertThat(result, is(notNullValue()));
            assertThat(result.link(), is(equalTo(cleanedLink)));
            assertThat(result.title(), is(nullValue()));
            assertThat(result.lang(), is(equalTo(SubtitlesLang.RU)));
            assertThat(result.subtitles(), containsString("Автосубтитры"));
        }

        @Test
        void when_load_withValidUrlAndEnglishManualSubtitles_then_success(@TempDir Path tempDir) throws Exception {
            String dirtyLink = "https://youtube.com/watch?v=abc";
            String cleanedLink = "https://youtube.com/watch?v=abc";
            String subtitlesContent = "WEBVTT\n\n00:00:01.000 --> 00:00:03.000\nHello world";

            Path subtitlesFile = tempDir.resolve("temp_subs_789.en.vtt");
            Files.writeString(subtitlesFile, subtitlesContent);

            ProcessBuilder listSubsProcessBuilder = createMockProcessBuilder(createListSubsOutput(false, false, true, false), "", 0);
            ProcessBuilder downloadProcessBuilder = createMockProcessBuilder("", "", 0);

            when(ytLinkHelper.isValidYoutubeUrl(dirtyLink)).thenReturn(true);
            when(ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(dirtyLink)).thenReturn(cleanedLink);
            when(ytDlpProcessBuilderCreator.createListSubsProcessBuilder(cleanedLink)).thenReturn(listSubsProcessBuilder);
            when(ytDlpProcessBuilderCreator.createProcessBuilder(cleanedLink, "en", false)).thenReturn(Pair.of(downloadProcessBuilder, subtitlesFile));

            YtSubtitles result = ytSubtitlesLoader.load(dirtyLink);

            assertThat(result.lang(), is(equalTo(SubtitlesLang.EN)));
            assertThat(result.subtitles(), containsString("Hello world"));
        }

        @Test
        void when_load_withValidUrlAndEnglishAutoSubtitles_then_success(@TempDir Path tempDir) throws Exception {
            String dirtyLink = "https://www.youtube.com/watch?v=xyz";
            String cleanedLink = "https://www.youtube.com/watch?v=xyz";
            String subtitlesContent = "WEBVTT\n\n00:00:01.000 --> 00:00:03.000\nAuto captions";

            Path subtitlesFile = tempDir.resolve("temp_subs_999.en.vtt");
            Files.writeString(subtitlesFile, subtitlesContent);

            ProcessBuilder listSubsProcessBuilder = createMockProcessBuilder(createListSubsOutput(false, false, false, true), "", 0);
            ProcessBuilder downloadProcessBuilder = createMockProcessBuilder("", "", 0);

            when(ytLinkHelper.isValidYoutubeUrl(dirtyLink)).thenReturn(true);
            when(ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(dirtyLink)).thenReturn(cleanedLink);
            when(ytDlpProcessBuilderCreator.createListSubsProcessBuilder(cleanedLink)).thenReturn(listSubsProcessBuilder);
            when(ytDlpProcessBuilderCreator.createProcessBuilder(cleanedLink, "en", true)).thenReturn(Pair.of(downloadProcessBuilder, subtitlesFile));

            YtSubtitles result = ytSubtitlesLoader.load(dirtyLink);

            assertThat(result.lang(), is(equalTo(SubtitlesLang.EN)));
            assertThat(result.subtitles(), containsString("Auto captions"));
        }

        @Test
        void when_load_withInvalidUrl_then_ytLoadingException() {
            String invalidLink = "https://invalid-url.com";

            when(ytLinkHelper.isValidYoutubeUrl(invalidLink)).thenReturn(false);

            YtLoadingException exception = assertThrows(YtLoadingException.class, () -> ytSubtitlesLoader.load(invalidLink));

            assertThat(exception.getId(), is(equalTo("824c")));
            assertThat(exception.getMessage(), containsString("Невалидная ссылка youtube"));
            verify(ytLinkHelper).isValidYoutubeUrl(invalidLink);
        }

        @Test
        void when_load_withNoSubtitlesAvailable_then_ytLoadingException() throws Exception {
            String dirtyLink = "https://www.youtube.com/watch?v=noSubs";
            String cleanedLink = "https://www.youtube.com/watch?v=noSubs";

            ProcessBuilder listSubsProcessBuilder = createMockProcessBuilder(createListSubsOutput(false, false, false, false), "", 0);

            when(ytLinkHelper.isValidYoutubeUrl(dirtyLink)).thenReturn(true);
            when(ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(dirtyLink)).thenReturn(cleanedLink);
            when(ytDlpProcessBuilderCreator.createListSubsProcessBuilder(cleanedLink)).thenReturn(listSubsProcessBuilder);

            YtLoadingException exception = assertThrows(YtLoadingException.class, () -> ytSubtitlesLoader.load(dirtyLink));

            assertThat(exception.getId(), is(equalTo("48ae")));
            assertThat(exception.getMessage(), containsString("Не удалось найти подходящие субтитры"));
        }

        @Test
        void when_load_withListSubsProcessError_then_ytLoadingException() throws Exception {
            String dirtyLink = "https://www.youtube.com/watch?v=error";
            String cleanedLink = "https://www.youtube.com/watch?v=error";

            ProcessBuilder listSubsProcessBuilder = createMockProcessBuilder("", "Error occurred", 1);

            when(ytLinkHelper.isValidYoutubeUrl(dirtyLink)).thenReturn(true);
            when(ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(dirtyLink)).thenReturn(cleanedLink);
            when(ytDlpProcessBuilderCreator.createListSubsProcessBuilder(cleanedLink)).thenReturn(listSubsProcessBuilder);

            YtLoadingException exception = assertThrows(YtLoadingException.class, () -> ytSubtitlesLoader.load(dirtyLink));

            assertThat(exception.getId(), is(equalTo("48ae")));
        }

        @Test
        void when_load_withDownloadProcessError_then_ytLoadingException(@TempDir Path tempDir) throws Exception {
            String dirtyLink = "https://www.youtube.com/watch?v=downloadError";
            String cleanedLink = "https://www.youtube.com/watch?v=downloadError";

            Path subtitlesFile = tempDir.resolve("temp_subs_error.ru.vtt");

            ProcessBuilder listSubsProcessBuilder = createMockProcessBuilder(createListSubsOutput(true, false, false, false), "", 0);
            ProcessBuilder downloadProcessBuilder = createMockProcessBuilder("", "Download failed", 1);

            when(ytLinkHelper.isValidYoutubeUrl(dirtyLink)).thenReturn(true);
            when(ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(dirtyLink)).thenReturn(cleanedLink);
            when(ytDlpProcessBuilderCreator.createListSubsProcessBuilder(cleanedLink)).thenReturn(listSubsProcessBuilder);
            when(ytDlpProcessBuilderCreator.createProcessBuilder(cleanedLink, "ru", false)).thenReturn(Pair.of(downloadProcessBuilder, subtitlesFile));

            YtLoadingException exception = assertThrows(YtLoadingException.class, () -> ytSubtitlesLoader.load(dirtyLink));

            assertThat(exception.getId(), is(equalTo("48ae")));
            assertThat(exception.getMessage(), containsString("Не удалось загрузить субтитры"));
        }

        @Test
        void when_load_withFileNotCreated_then_ytLoadingException(@TempDir Path tempDir) throws Exception {
            String dirtyLink = "https://www.youtube.com/watch?v=noFile";
            String cleanedLink = "https://www.youtube.com/watch?v=noFile";

            Path subtitlesFile = tempDir.resolve("non_existent.ru.vtt");

            ProcessBuilder listSubsProcessBuilder = createMockProcessBuilder(createListSubsOutput(true, false, false, false), "", 0);
            ProcessBuilder downloadProcessBuilder = createMockProcessBuilder("", "", 0);

            when(ytLinkHelper.isValidYoutubeUrl(dirtyLink)).thenReturn(true);
            when(ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(dirtyLink)).thenReturn(cleanedLink);
            when(ytDlpProcessBuilderCreator.createListSubsProcessBuilder(cleanedLink)).thenReturn(listSubsProcessBuilder);
            when(ytDlpProcessBuilderCreator.createProcessBuilder(cleanedLink, "ru", false)).thenReturn(Pair.of(downloadProcessBuilder, subtitlesFile));

            YtLoadingException exception = assertThrows(YtLoadingException.class, () -> ytSubtitlesLoader.load(dirtyLink));

            assertThat(exception.getId(), is(equalTo("48ae")));
        }

        @Test
        void when_load_withEmptySubtitlesContent_then_ytLoadingException(@TempDir Path tempDir) throws Exception {
            String dirtyLink = "https://www.youtube.com/watch?v=empty";
            String cleanedLink = "https://www.youtube.com/watch?v=empty";

            Path subtitlesFile = tempDir.resolve("temp_subs_empty.ru.vtt");
            Files.writeString(subtitlesFile, "   ");

            ProcessBuilder listSubsProcessBuilder = createMockProcessBuilder(createListSubsOutput(true, false, false, false), "", 0);
            ProcessBuilder downloadProcessBuilder = createMockProcessBuilder("", "", 0);

            when(ytLinkHelper.isValidYoutubeUrl(dirtyLink)).thenReturn(true);
            when(ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(dirtyLink)).thenReturn(cleanedLink);
            when(ytDlpProcessBuilderCreator.createListSubsProcessBuilder(cleanedLink)).thenReturn(listSubsProcessBuilder);
            when(ytDlpProcessBuilderCreator.createProcessBuilder(cleanedLink, "ru", false)).thenReturn(Pair.of(downloadProcessBuilder, subtitlesFile));

            YtLoadingException exception = assertThrows(YtLoadingException.class, () -> ytSubtitlesLoader.load(dirtyLink));

            assertThat(exception.getId(), is(equalTo("48ae")));
        }

        @Test
        void when_load_withSubtitlesContainingDuplicates_then_duplicatesRemoved(@TempDir Path tempDir) throws Exception {
            String dirtyLink = "https://www.youtube.com/watch?v=duplicates";
            String cleanedLink = "https://www.youtube.com/watch?v=duplicates";
            String subtitlesContent = "WEBVTT\n\n00:00:01.000 --> 00:00:03.000\nСтрока один\n\n00:00:04.000 --> 00:00:06.000\nСтрока один\n\n00:00:07.000 --> 00:00:09.000\nСтрока два";

            Path subtitlesFile = tempDir.resolve("temp_subs_dup.ru.vtt");
            Files.writeString(subtitlesFile, subtitlesContent);

            ProcessBuilder listSubsProcessBuilder = createMockProcessBuilder(createListSubsOutput(true, false, false, false), "", 0);
            ProcessBuilder downloadProcessBuilder = createMockProcessBuilder("", "", 0);

            when(ytLinkHelper.isValidYoutubeUrl(dirtyLink)).thenReturn(true);
            when(ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(dirtyLink)).thenReturn(cleanedLink);
            when(ytDlpProcessBuilderCreator.createListSubsProcessBuilder(cleanedLink)).thenReturn(listSubsProcessBuilder);
            when(ytDlpProcessBuilderCreator.createProcessBuilder(cleanedLink, "ru", false)).thenReturn(Pair.of(downloadProcessBuilder, subtitlesFile));

            YtSubtitles result = ytSubtitlesLoader.load(dirtyLink);

            int firstOccurrence = result.subtitles().indexOf("Строка один");
            int lastOccurrence = result.subtitles().lastIndexOf("Строка один");
            assertThat(firstOccurrence, is(equalTo(lastOccurrence)));
        }

        @Test
        void when_load_withSubtitlesContainingVttTags_then_tagsRemoved(@TempDir Path tempDir) throws Exception {
            String dirtyLink = "https://www.youtube.com/watch?v=tags";
            String cleanedLink = "https://www.youtube.com/watch?v=tags";
            String subtitlesContent = "WEBVTT\n\n00:00:01.000 --> 00:00:03.000 align:start position:0%\n<c>Text with tags</c>\n\n00:00:04.000 --> 00:00:06.000\n<v Speaker>Another line</v>";

            Path subtitlesFile = tempDir.resolve("temp_subs_tags.ru.vtt");
            Files.writeString(subtitlesFile, subtitlesContent);

            ProcessBuilder listSubsProcessBuilder = createMockProcessBuilder(createListSubsOutput(true, false, false, false), "", 0);
            ProcessBuilder downloadProcessBuilder = createMockProcessBuilder("", "", 0);

            when(ytLinkHelper.isValidYoutubeUrl(dirtyLink)).thenReturn(true);
            when(ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(dirtyLink)).thenReturn(cleanedLink);
            when(ytDlpProcessBuilderCreator.createListSubsProcessBuilder(cleanedLink)).thenReturn(listSubsProcessBuilder);
            when(ytDlpProcessBuilderCreator.createProcessBuilder(cleanedLink, "ru", false)).thenReturn(Pair.of(downloadProcessBuilder, subtitlesFile));

            YtSubtitles result = ytSubtitlesLoader.load(dirtyLink);

            assertThat(result.subtitles(), containsString("Text with tags"));
            assertThat(result.subtitles(), containsString("Another line"));
            assertThat(result.subtitles().contains("<c>"), is(false));
            assertThat(result.subtitles().contains("<v"), is(false));
        }

        @Test
        void when_load_withProcessBuilderThrowingIOException_then_ytLoadingException() throws Exception {
            String dirtyLink = "https://www.youtube.com/watch?v=ioerror";
            String cleanedLink = "https://www.youtube.com/watch?v=ioerror";

            ProcessBuilder listSubsProcessBuilder = mock(ProcessBuilder.class);

            when(ytLinkHelper.isValidYoutubeUrl(dirtyLink)).thenReturn(true);
            when(ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(dirtyLink)).thenReturn(cleanedLink);
            when(ytDlpProcessBuilderCreator.createListSubsProcessBuilder(cleanedLink)).thenReturn(listSubsProcessBuilder);
            when(listSubsProcessBuilder.start()).thenThrow(new IOException("Process failed to start"));

            YtLoadingException exception = assertThrows(YtLoadingException.class, () -> ytSubtitlesLoader.load(dirtyLink));

            assertThat(exception.getId(), is(equalTo("48ae")));
            assertThat(exception.getMessage(), containsString("Не удалось найти подходящие субтитры"));
        }

        @Test
        void when_load_withMultipleSubtitleLanguagesAvailable_then_russianManualPreferred(@TempDir Path tempDir) throws Exception {
            String dirtyLink = "https://www.youtube.com/watch?v=multi";
            String cleanedLink = "https://www.youtube.com/watch?v=multi";
            String subtitlesContent = "WEBVTT\n\n00:00:01.000 --> 00:00:03.000\nРусский текст";

            Path subtitlesFile = tempDir.resolve("temp_subs_multi.ru.vtt");
            Files.writeString(subtitlesFile, subtitlesContent);

            ProcessBuilder listSubsProcessBuilder = createMockProcessBuilder(createListSubsOutput(true, true, true, true), "", 0);
            ProcessBuilder downloadProcessBuilder = createMockProcessBuilder("", "", 0);

            when(ytLinkHelper.isValidYoutubeUrl(dirtyLink)).thenReturn(true);
            when(ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(dirtyLink)).thenReturn(cleanedLink);
            when(ytDlpProcessBuilderCreator.createListSubsProcessBuilder(cleanedLink)).thenReturn(listSubsProcessBuilder);
            when(ytDlpProcessBuilderCreator.createProcessBuilder(cleanedLink, "ru", false)).thenReturn(Pair.of(downloadProcessBuilder, subtitlesFile));

            YtSubtitles result = ytSubtitlesLoader.load(dirtyLink);

            assertThat(result.lang(), is(equalTo(SubtitlesLang.RU)));
            verify(ytDlpProcessBuilderCreator).createProcessBuilder(eq(cleanedLink), eq("ru"), eq(false));
        }

        @Test
        void when_load_withOnlyNonVttFormats_then_ytLoadingException() throws Exception {
            String dirtyLink = "https://www.youtube.com/watch?v=novtt";
            String cleanedLink = "https://www.youtube.com/watch?v=novtt";

            String listSubsOutput = "Available subtitles for dQw4w9WgXcQ:\nLanguage  formats\nru        json3, srv1, srv2, srv3, ttml\nen        json3, srv1, srv2, srv3, ttml";
            ProcessBuilder listSubsProcessBuilder = createMockProcessBuilder(listSubsOutput, "", 0);

            when(ytLinkHelper.isValidYoutubeUrl(dirtyLink)).thenReturn(true);
            when(ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(dirtyLink)).thenReturn(cleanedLink);
            when(ytDlpProcessBuilderCreator.createListSubsProcessBuilder(cleanedLink)).thenReturn(listSubsProcessBuilder);

            YtLoadingException exception = assertThrows(YtLoadingException.class, () -> ytSubtitlesLoader.load(dirtyLink));

            assertThat(exception.getId(), is(equalTo("48ae")));
        }

        @Test
        void when_load_withStderrWarningButSuccess_then_success(@TempDir Path tempDir) throws Exception {
            String dirtyLink = "https://www.youtube.com/watch?v=warning";
            String cleanedLink = "https://www.youtube.com/watch?v=warning";
            String subtitlesContent = "WEBVTT\n\n00:00:01.000 --> 00:00:03.000\nТекст";

            Path subtitlesFile = tempDir.resolve("temp_subs_warn.ru.vtt");
            Files.writeString(subtitlesFile, subtitlesContent);

            ProcessBuilder listSubsProcessBuilder = createMockProcessBuilder(createListSubsOutput(true, false, false, false), "WARNING: Some warning message", 0);
            ProcessBuilder downloadProcessBuilder = createMockProcessBuilder("", "WARNING: Another warning", 0);

            when(ytLinkHelper.isValidYoutubeUrl(dirtyLink)).thenReturn(true);
            when(ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(dirtyLink)).thenReturn(cleanedLink);
            when(ytDlpProcessBuilderCreator.createListSubsProcessBuilder(cleanedLink)).thenReturn(listSubsProcessBuilder);
            when(ytDlpProcessBuilderCreator.createProcessBuilder(cleanedLink, "ru", false)).thenReturn(Pair.of(downloadProcessBuilder, subtitlesFile));

            YtSubtitles result = ytSubtitlesLoader.load(dirtyLink);

            assertThat(result, is(notNullValue()));
            assertThat(result.lang(), is(equalTo(SubtitlesLang.RU)));
        }

        @Test
        void when_load_withLanguageVariant_then_success(@TempDir Path tempDir) throws Exception {
            String dirtyLink = "https://www.youtube.com/watch?v=variant";
            String cleanedLink = "https://www.youtube.com/watch?v=variant";
            String subtitlesContent = "WEBVTT\n\n00:00:01.000 --> 00:00:03.000\nBritish English";

            Path subtitlesFile = tempDir.resolve("temp_subs_var.en.vtt");
            Files.writeString(subtitlesFile, subtitlesContent);

            String listSubsOutput = "Available subtitles for dQw4w9WgXcQ:\nLanguage  formats\nen-GB     vtt, json3, srv1";
            ProcessBuilder listSubsProcessBuilder = createMockProcessBuilder(listSubsOutput, "", 0);
            ProcessBuilder downloadProcessBuilder = createMockProcessBuilder("", "", 0);

            when(ytLinkHelper.isValidYoutubeUrl(dirtyLink)).thenReturn(true);
            when(ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(dirtyLink)).thenReturn(cleanedLink);
            when(ytDlpProcessBuilderCreator.createListSubsProcessBuilder(cleanedLink)).thenReturn(listSubsProcessBuilder);
            when(ytDlpProcessBuilderCreator.createProcessBuilder(cleanedLink, "en", false)).thenReturn(Pair.of(downloadProcessBuilder, subtitlesFile));

            YtSubtitles result = ytSubtitlesLoader.load(dirtyLink);

            assertThat(result.lang(), is(equalTo(SubtitlesLang.EN)));
        }

        @Test
        void when_load_withNoSubtitlesRequestedLanguages_then_ytLoadingException(@TempDir Path tempDir) throws Exception {
            String dirtyLink = "https://www.youtube.com/watch?v=nosubs";
            String cleanedLink = "https://www.youtube.com/watch?v=nosubs";

            Path subtitlesFile = tempDir.resolve("temp_subs_nosubs.ru.vtt");

            ProcessBuilder listSubsProcessBuilder = createMockProcessBuilder(createListSubsOutput(true, false, false, false), "", 0);
            ProcessBuilder downloadProcessBuilder = createMockProcessBuilder("", "There are no subtitles for the requested languages", 0);

            when(ytLinkHelper.isValidYoutubeUrl(dirtyLink)).thenReturn(true);
            when(ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(dirtyLink)).thenReturn(cleanedLink);
            when(ytDlpProcessBuilderCreator.createListSubsProcessBuilder(cleanedLink)).thenReturn(listSubsProcessBuilder);
            when(ytDlpProcessBuilderCreator.createProcessBuilder(cleanedLink, "ru", false)).thenReturn(Pair.of(downloadProcessBuilder, subtitlesFile));

            YtLoadingException exception = assertThrows(YtLoadingException.class, () -> ytSubtitlesLoader.load(dirtyLink));

            assertThat(exception.getId(), is(equalTo("48ae")));
        }
    }

    private ProcessBuilder createMockProcessBuilder(String stdout, String stderr, int exitCode) throws Exception {
        ProcessBuilder processBuilder = mock(ProcessBuilder.class);
        Process process = mock(Process.class);

        InputStream stdoutStream = new ByteArrayInputStream(stdout.getBytes());
        InputStream stderrStream = new ByteArrayInputStream(stderr.getBytes());

        when(processBuilder.start()).thenReturn(process);
        when(process.getInputStream()).thenReturn(stdoutStream);
        when(process.getErrorStream()).thenReturn(stderrStream);
        when(process.waitFor()).thenReturn(exitCode);

        return processBuilder;
    }

    private String createListSubsOutput(boolean ruManual, boolean ruAuto, boolean enManual, boolean enAuto) {
        StringBuilder output = new StringBuilder();

        if (ruManual || enManual) {
            output.append("Available subtitles for dQw4w9WgXcQ:\n");
            output.append("Language  formats\n");
            if (ruManual) {
                output.append("ru        vtt, json3, srv1, srv2, srv3, ttml\n");
            }
            if (enManual) {
                output.append("en        vtt, json3, srv1, srv2, srv3, ttml\n");
            }
        }

        if (ruAuto || enAuto) {
            output.append("Automatic captions:\n");
            output.append("Language  formats\n");
            if (ruAuto) {
                output.append("ru        vtt, json3, srv1, srv2, srv3, ttml\n");
            }
            if (enAuto) {
                output.append("en        vtt, json3, srv1, srv2, srv3, ttml\n");
            }
        }

        return output.toString();
    }
}