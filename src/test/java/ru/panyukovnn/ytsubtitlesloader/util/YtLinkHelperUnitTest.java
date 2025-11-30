package ru.panyukovnn.ytsubtitlesloader.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.panyukovnn.ytsubtitlesloader.exception.YtLoadingException;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

class YtLinkHelperUnitTest {

    @Nested
    class RemoveRedundantQueryParamsMethod {

        @Test
        void when_removeRedundantQueryParamsFromYoutubeLint_withUrlWithoutQueryParams_then_returnUrlUnchanged() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String inputUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";

            String result = ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(inputUrl);

            assertThat(result, equalTo(inputUrl));
        }

        @Test
        void when_removeRedundantQueryParamsFromYoutubeLint_withUrlHavingOnlyVParameter_then_returnUrlWithVParameterPreserved() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String inputUrl = "https://youtube.com/watch?v=abc1234567";

            String result = ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(inputUrl);

            assertThat(result, containsString("v=abc1234567"));
        }

        @Test
        void when_removeRedundantQueryParamsFromYoutubeLint_withUrlHavingMultipleRedundantParameters_then_removeAllButV() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String inputUrl = "https://youtube.com/watch?v=dQw4w9WgXcQ&t=10&list=PLxyz&index=5";

            String result = ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(inputUrl);

            assertThat(result, allOf(
                containsString("v=dQw4w9WgXcQ"),
                not(containsString("t=")),
                not(containsString("list=")),
                not(containsString("index="))
            ));
        }

        @Test
        void when_removeRedundantQueryParamsFromYoutubeLint_withUrlHavingVParameterInDifferentPosition_then_extractAndRetainV() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String inputUrl = "https://youtube.com/watch?t=10&v=dQw4w9WgXcQ&list=xyz";

            String result = ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(inputUrl);

            assertThat(result, allOf(
                containsString("v=dQw4w9WgXcQ"),
                not(containsString("t=")),
                not(containsString("list="))
            ));
        }

        @Test
        void when_removeRedundantQueryParamsFromYoutubeLint_withUrlMissingVParameter_then_returnUrlWithNullQuery() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String inputUrl = "https://youtube.com/watch?t=10&list=xyz";

            String result = ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(inputUrl);

            assertThat(result, not(containsString("v=")));
        }

        @Test
        void when_removeRedundantQueryParamsFromYoutubeLint_withUrlHavingFragmentAndQuery_then_preserveFragmentAndRemoveRedundantParams() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String inputUrl = "https://youtube.com/watch?v=abc1234567&t=10#comments";

            String result = ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(inputUrl);

            assertThat(result, allOf(
                containsString("v=abc1234567"),
                containsString("#comments"),
                not(containsString("t="))
            ));
        }

        @Test
        void when_removeRedundantQueryParamsFromYoutubeLint_withMalformedUrl_then_throwYtLoadingExceptionWith4bc5Id() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String malformedUrl = "https://youtube.com/watch?v=test&param=%%invalid";

            YtLoadingException exception = assertThrows(YtLoadingException.class, () ->
                ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(malformedUrl)
            );

            assertThat(exception.getId(), equalTo("4bc5"));
            assertThat(exception.getMessage(), containsString("Невалидная ссылка youtube"));
        }

        @Test
        void when_removeRedundantQueryParamsFromYoutubeLint_withUrlHavingEmptyVParameterValue_then_removeVParameter() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String inputUrl = "https://youtube.com/watch?v=&t=10";

            String result = ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(inputUrl);

            assertThat(result, not(containsString("t=")));
        }

        @Test
        void when_removeRedundantQueryParamsFromYoutubeLint_withUrlHavingDuplicateVParameters_then_useFirstVParameterValue() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String inputUrl = "https://youtube.com/watch?v=first&v=second&t=10";

            String result = ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(inputUrl);

            assertThat(result, allOf(
                containsString("v=first"),
                not(containsString("second")),
                not(containsString("t="))
            ));
        }

        @Test
        void when_removeRedundantQueryParamsFromYoutubeLint_withUrlHavingSpecialCharactersInVideoId_then_preserveSpecialCharactersInVParameter() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String inputUrl = "https://youtube.com/watch?v=abc-def_ghi&t=10";

            String result = ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(inputUrl);

            assertThat(result, allOf(
                containsString("v=abc-def_ghi"),
                not(containsString("t="))
            ));
        }

        @Test
        void when_removeRedundantQueryParamsFromYoutubeLint_withUrlHavingEncodedCharactersInVideoId_then_preserveEncoding() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String inputUrl = "https://youtube.com/watch?v=abc%20def&t=10";

            String result = ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(inputUrl);

            assertThat(result, allOf(
                containsString("v=abc%2520def"),
                not(containsString("t="))
            ));
        }

        @Test
        void when_removeRedundantQueryParamsFromYoutubeLint_withShortUrlFormat_then_returnUrlUnchanged() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String inputUrl = "https://youtu.be/dQw4w9WgXcQ";

            String result = ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(inputUrl);

            assertThat(result, equalTo(inputUrl));
        }

        @Test
        void when_removeRedundantQueryParamsFromYoutubeLint_withShortUrlContainingQueryParams_then_removeRedundantParams() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String inputUrl = "https://youtu.be/dQw4w9WgXcQ?v=abc&t=10";

            String result = ytLinkHelper.removeRedundantQueryParamsFromYoutubeLint(inputUrl);

            assertThat(result, not(containsString("t=")));
        }
    }

    @Nested
    class IsValidYoutubeUrlMethod {

        @Test
        void when_isValidYoutubeUrl_withValidWatchUrl_then_returnTrue() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";

            boolean result = ytLinkHelper.isValidYoutubeUrl(url);

            assertTrue(result);
        }

        @Test
        void when_isValidYoutubeUrl_withValidShortUrl_then_returnTrue() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String url = "https://youtu.be/dQw4w9WgXcQ";

            boolean result = ytLinkHelper.isValidYoutubeUrl(url);

            assertTrue(result);
        }

        @Test
        void when_isValidYoutubeUrl_withValidShortsUrl_then_returnTrue() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String url = "https://www.youtube.com/shorts/abc12345678";

            boolean result = ytLinkHelper.isValidYoutubeUrl(url);

            assertTrue(result);
        }

        @Test
        void when_isValidYoutubeUrl_withValidLiveUrl_then_returnTrue() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String url = "https://www.youtube.com/live/abc12345678";

            boolean result = ytLinkHelper.isValidYoutubeUrl(url);

            assertTrue(result);
        }

        @Test
        void when_isValidYoutubeUrl_withValidSubdomainUrl_then_returnTrue() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String url = "https://m.youtube.com/watch?v=dQw4w9WgXcQ";

            boolean result = ytLinkHelper.isValidYoutubeUrl(url);

            assertTrue(result);
        }

        @Test
        void when_isValidYoutubeUrl_withInvalidVideoId_then_returnFalse() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String url = "https://youtube.com/watch?v=abc";

            boolean result = ytLinkHelper.isValidYoutubeUrl(url);

            assertFalse(result);
        }

        @Test
        void when_isValidYoutubeUrl_withInvalidVideoIdCharacters_then_returnFalse() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String url = "https://youtube.com/watch?v=abc@#$%^&*()";

            boolean result = ytLinkHelper.isValidYoutubeUrl(url);

            assertFalse(result);
        }

        @Test
        void when_isValidYoutubeUrl_withWatchUrlMissingVParameter_then_returnFalse() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String url = "https://youtube.com/watch?t=10";

            boolean result = ytLinkHelper.isValidYoutubeUrl(url);

            assertFalse(result);
        }

        @Test
        void when_isValidYoutubeUrl_withWatchUrlWithoutQueryString_then_returnFalse() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String url = "https://youtube.com/watch";

            boolean result = ytLinkHelper.isValidYoutubeUrl(url);

            assertFalse(result);
        }

        @Test
        void when_isValidYoutubeUrl_withNonYoutubeHost_then_returnFalse() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String url = "https://example.com/watch?v=dQw4w9WgXcQ";

            boolean result = ytLinkHelper.isValidYoutubeUrl(url);

            assertFalse(result);
        }

        @Test
        void when_isValidYoutubeUrl_withFakeYoutubeHost_then_returnFalse() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String url = "https://fakeyoutube.com/watch?v=dQw4w9WgXcQ";

            boolean result = ytLinkHelper.isValidYoutubeUrl(url);

            assertFalse(result);
        }

        @Test
        void when_isValidYoutubeUrl_withHostStartingWithDot_then_returnFalse() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String url = "https://.youtube.com/watch?v=dQw4w9WgXcQ";

            boolean result = ytLinkHelper.isValidYoutubeUrl(url);

            assertFalse(result);
        }

        @Test
        void when_isValidYoutubeUrl_withHostContainingInvalidCharacters_then_returnFalse() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String url = "https://you@tube.com/watch?v=dQw4w9WgXcQ";

            boolean result = ytLinkHelper.isValidYoutubeUrl(url);

            assertFalse(result);
        }

        @Test
        void when_isValidYoutubeUrl_withMalformedUrl_then_returnFalse() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String url = "not a valid url at all";

            boolean result = ytLinkHelper.isValidYoutubeUrl(url);

            assertFalse(result);
        }

        @Test
        void when_isValidYoutubeUrl_withNullUrl_then_returnFalse() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String url = null;

            boolean result = ytLinkHelper.isValidYoutubeUrl(url);

            assertFalse(result);
        }

        @Test
        void when_isValidYoutubeUrl_withEmptyUrl_then_returnFalse() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String url = "";

            boolean result = ytLinkHelper.isValidYoutubeUrl(url);

            assertFalse(result);
        }

        @Test
        void when_isValidYoutubeUrl_withValidUrlIgnoresCase_then_returnTrue() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String url = "https://WWW.YOUTUBE.COM/watch?v=dQw4w9WgXcQ";

            boolean result = ytLinkHelper.isValidYoutubeUrl(url);

            assertTrue(result);
        }

        @Test
        void when_isValidYoutubeUrl_withShortsUrlMissingVideoId_then_returnFalse() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String url = "https://youtube.com/shorts/abc";

            boolean result = ytLinkHelper.isValidYoutubeUrl(url);

            assertFalse(result);
        }

        @Test
        void when_isValidYoutubeUrl_withLiveUrlMissingStreamId_then_returnFalse() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String url = "https://youtube.com/live/invalid";

            boolean result = ytLinkHelper.isValidYoutubeUrl(url);

            assertFalse(result);
        }

        @Test
        void when_isValidYoutubeUrl_withShortUrlHavingInvalidIdLength_then_returnFalse() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String url = "https://youtu.be/abc12";

            boolean result = ytLinkHelper.isValidYoutubeUrl(url);

            assertFalse(result);
        }

        @Test
        void when_isValidYoutubeUrl_withValidYoutubeDomainAsSubdomain_then_returnTrue() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String url = "https://en.youtube.com/watch?v=dQw4w9WgXcQ";

            boolean result = ytLinkHelper.isValidYoutubeUrl(url);

            assertTrue(result);
        }

        @Test
        void when_isValidYoutubeUrl_withHttpProtocol_then_returnTrue() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String url = "http://youtube.com/watch?v=dQw4w9WgXcQ";

            boolean result = ytLinkHelper.isValidYoutubeUrl(url);

            assertTrue(result);
        }

        @Test
        void when_isValidYoutubeUrl_withValidVideoIdExactly11Chars_then_returnTrue() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String url = "https://youtube.com/watch?v=12345678901";

            boolean result = ytLinkHelper.isValidYoutubeUrl(url);

            assertTrue(result);
        }

        @Test
        void when_isValidYoutubeUrl_withShortUrlPathMissingSlash_then_returnFalse() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String url = "https://youtu.be/";

            boolean result = ytLinkHelper.isValidYoutubeUrl(url);

            assertFalse(result);
        }
    }

    @Nested
    class RegexPatternValidation {

        @Test
        void when_isValidYoutubeUrl_withValidVideoIdContainingHyphen_then_returnTrue() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String url = "https://youtube.com/watch?v=abc-defg-hi";

            boolean result = ytLinkHelper.isValidYoutubeUrl(url);

            assertTrue(result);
        }

        @Test
        void when_isValidYoutubeUrl_withValidVideoIdContainingUnderscore_then_returnTrue() {
            YtLinkHelper ytLinkHelper = new YtLinkHelper();
            String url = "https://youtube.com/watch?v=abc_defg_hi";

            boolean result = ytLinkHelper.isValidYoutubeUrl(url);

            assertTrue(result);
        }
    }
}
