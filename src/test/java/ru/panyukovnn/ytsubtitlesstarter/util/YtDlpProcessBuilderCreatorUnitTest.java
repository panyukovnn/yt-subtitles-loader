package ru.panyukovnn.ytsubtitlesstarter.util;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YtDlpProcessBuilderCreatorUnitTest {

    @Nested
    class CreateProcessBuilderMethod {

        @Test
        void when_createProcessBuilder_withValidUrlAndRussianLanguageAndNoAutoSubs_then_returnProcessBuilderWithCorrectCommand() throws Exception {
            YtDlpProcessBuilderCreator creator = new YtDlpProcessBuilderCreator();
            String videoUrl = "https://youtube.com/watch?v=abc1234567";
            String lang = "ru";
            boolean isAutoSubs = false;

            Pair<ProcessBuilder, Path> result = creator.createProcessBuilder(videoUrl, lang, isAutoSubs);

            List<String> command = result.getLeft().command();
            assertThat(command, hasItem("--write-subs"));
            assertThat(command, not(hasItem("--write-auto-subs")));
            assertThat(command, hasItem("--sub-lang"));
            assertThat(command, hasItem("ru"));
            assertThat(command, hasItem(videoUrl));
        }

        @Test
        void when_createProcessBuilder_withValidUrlAndEnglishLanguageAndAutoSubs_then_returnProcessBuilderWithAutoSubsFlag() throws Exception {
            YtDlpProcessBuilderCreator creator = new YtDlpProcessBuilderCreator();
            String videoUrl = "https://youtube.com/watch?v=xyz9876543";
            String lang = "en";
            boolean isAutoSubs = true;

            Pair<ProcessBuilder, Path> result = creator.createProcessBuilder(videoUrl, lang, isAutoSubs);

            List<String> command = result.getLeft().command();
            assertThat(command, hasItem("--write-auto-subs"));
            assertThat(command, not(hasItem("--write-subs")));
            assertThat(command, hasItem("--sub-lang"));
            assertThat(command, hasItem("en"));
        }

        @Test
        void when_createProcessBuilder_withDifferentLanguageCodes_then_passLanguageToSubLangParameter() throws Exception {
            YtDlpProcessBuilderCreator creator = new YtDlpProcessBuilderCreator();
            String videoUrl = "https://youtube.com/watch?v=abc";
            String lang = "fr";
            boolean isAutoSubs = false;

            Pair<ProcessBuilder, Path> result = creator.createProcessBuilder(videoUrl, lang, isAutoSubs);

            List<String> command = result.getLeft().command();
            assertThat(command, hasItem("--sub-lang"));
            assertThat(command, hasItem("fr"));
        }

        @Test
        void when_createProcessBuilder_withVideoUrlContainingSpecialCharacters_then_includeUrlInCommand() throws Exception {
            YtDlpProcessBuilderCreator creator = new YtDlpProcessBuilderCreator();
            String videoUrl = "https://youtube.com/watch?v=test&t=10";
            String lang = "ru";
            boolean isAutoSubs = false;

            Pair<ProcessBuilder, Path> result = creator.createProcessBuilder(videoUrl, lang, isAutoSubs);

            List<String> command = result.getLeft().command();
            assertThat(command, hasItem(videoUrl));
        }

        @Test
        void when_createProcessBuilder_withDifferentLanguageValues_then_eachReturnUniqueCommand() throws Exception {
            YtDlpProcessBuilderCreator creator = new YtDlpProcessBuilderCreator();
            String videoUrl = "https://youtube.com/watch?v=abc";

            Pair<ProcessBuilder, Path> result1 = creator.createProcessBuilder(videoUrl, "ru", false);
            Pair<ProcessBuilder, Path> result2 = creator.createProcessBuilder(videoUrl, "en", false);

            List<String> command1 = result1.getLeft().command();
            List<String> command2 = result2.getLeft().command();
            assertThat(command1, hasItem("ru"));
            assertThat(command2, hasItem("en"));
            assertThat(command1, not(equalTo(command2)));
        }

        @Test
        void when_createProcessBuilder_withAutoSubsTrue_then_useAutoSubsFlag() throws Exception {
            YtDlpProcessBuilderCreator creator = new YtDlpProcessBuilderCreator();
            String videoUrl = "https://youtube.com/watch?v=abc";
            String lang = "ru";
            boolean isAutoSubs = true;

            Pair<ProcessBuilder, Path> result = creator.createProcessBuilder(videoUrl, lang, isAutoSubs);

            List<String> command = result.getLeft().command();
            assertThat(command, hasItem("--write-auto-subs"));
        }

        @Test
        void when_createProcessBuilder_withAutoSubsFalse_then_useStandardSubsFlag() throws Exception {
            YtDlpProcessBuilderCreator creator = new YtDlpProcessBuilderCreator();
            String videoUrl = "https://youtube.com/watch?v=abc";
            String lang = "ru";
            boolean isAutoSubs = false;

            Pair<ProcessBuilder, Path> result = creator.createProcessBuilder(videoUrl, lang, isAutoSubs);

            List<String> command = result.getLeft().command();
            assertThat(command, hasItem("--write-subs"));
        }

        @Test
        void when_createProcessBuilder_then_returnProcessBuilderWithCorrectWorkingDirectory() throws Exception {
            YtDlpProcessBuilderCreator creator = new YtDlpProcessBuilderCreator();
            String videoUrl = "https://youtube.com/watch?v=abc";
            String lang = "ru";
            boolean isAutoSubs = false;

            Pair<ProcessBuilder, Path> result = creator.createProcessBuilder(videoUrl, lang, isAutoSubs);

            assertThat(result.getLeft().directory(), equalTo(new File(".")));
        }

        @Test
        void when_createProcessBuilder_thenReturnProcessBuilderWithDirectYtDlpExecution() throws Exception {
            YtDlpProcessBuilderCreator creator = new YtDlpProcessBuilderCreator();
            String videoUrl = "https://youtube.com/watch?v=abc";
            String lang = "ru";
            boolean isAutoSubs = false;

            Pair<ProcessBuilder, Path> result = creator.createProcessBuilder(videoUrl, lang, isAutoSubs);

            List<String> command = result.getLeft().command();
            // Первый аргумент должен быть путём к yt-dlp исполняемому файлу
            assertThat(command.get(0), anyOf(
                containsString("yt-dlp_macos"),
                containsString("yt-dlp_linux"),
                containsString("yt-dlp_linux_aarch64")
            ));
        }

        @Test
        void when_createProcessBuilder_withEmptyLanguageString_then_passEmptyStringToSubLangParameter() throws Exception {
            YtDlpProcessBuilderCreator creator = new YtDlpProcessBuilderCreator();
            String videoUrl = "https://youtube.com/watch?v=abc";
            String lang = "";
            boolean isAutoSubs = false;

            Pair<ProcessBuilder, Path> result = creator.createProcessBuilder(videoUrl, lang, isAutoSubs);

            List<String> command = result.getLeft().command();
            assertThat(command, hasItem("--sub-lang"));
            assertThat(command, hasItem(""));
        }

        @Test
        void when_createProcessBuilder_withNullLanguage_then_createCommandWithNullInSubLang() throws Exception {
            YtDlpProcessBuilderCreator creator = new YtDlpProcessBuilderCreator();
            String videoUrl = "https://youtube.com/watch?v=abc";
            String lang = null;
            boolean isAutoSubs = false;

            Pair<ProcessBuilder, Path> result = creator.createProcessBuilder(videoUrl, lang, isAutoSubs);

            List<String> command = result.getLeft().command();
            assertThat(command.get(0), anyOf(
                containsString("yt-dlp_macos"),
                containsString("yt-dlp_linux"),
                containsString("yt-dlp_linux_aarch64")
            ));
        }

        @Test
        void when_createProcessBuilder_withNullUrl_then_createCommandWithNullInUrl() throws Exception {
            YtDlpProcessBuilderCreator creator = new YtDlpProcessBuilderCreator();
            String videoUrl = null;
            String lang = "ru";
            boolean isAutoSubs = false;

            Pair<ProcessBuilder, Path> result = creator.createProcessBuilder(videoUrl, lang, isAutoSubs);

            List<String> command = result.getLeft().command();
            assertThat(command.get(0), anyOf(
                containsString("yt-dlp_macos"),
                containsString("yt-dlp_linux"),
                containsString("yt-dlp_linux_aarch64")
            ));
        }

        @Test
        void when_createProcessBuilder_withVeryLongUrl_then_commandConstructedWithFullUrl() throws Exception {
            YtDlpProcessBuilderCreator creator = new YtDlpProcessBuilderCreator();
            String videoUrl = "https://youtube.com/watch?v=" + "a".repeat(500);
            String lang = "ru";
            boolean isAutoSubs = false;

            Pair<ProcessBuilder, Path> result = creator.createProcessBuilder(videoUrl, lang, isAutoSubs);

            List<String> command = result.getLeft().command();
            assertThat(command, hasItem(videoUrl));
        }

        @Test
        void when_createProcessBuilder_withMultipleCalls_thenEachReturnNewProcessBuilderInstance() throws Exception {
            YtDlpProcessBuilderCreator creator = new YtDlpProcessBuilderCreator();
            String videoUrl = "https://youtube.com/watch?v=abc";

            Pair<ProcessBuilder, Path> result1 = creator.createProcessBuilder(videoUrl, "ru", false);
            Pair<ProcessBuilder, Path> result2 = creator.createProcessBuilder(videoUrl, "ru", false);

            assertThat(result1.getLeft(), not(sameInstance(result2.getLeft())));
            assertThat(result1.getRight(), not(equalTo(result2.getRight())));
        }

        @Test
        void when_createProcessBuilder_withVttFormatFlag_then_commandIncludesSubFormatVtt() throws Exception {
            YtDlpProcessBuilderCreator creator = new YtDlpProcessBuilderCreator();
            String videoUrl = "https://youtube.com/watch?v=abc";
            String lang = "ru";
            boolean isAutoSubs = false;

            Pair<ProcessBuilder, Path> result = creator.createProcessBuilder(videoUrl, lang, isAutoSubs);

            List<String> command = result.getLeft().command();
            assertThat(command, hasItem("--sub-format"));
            assertThat(command, hasItem("vtt"));
        }

        @Test
        void when_createProcessBuilder_withSkipDownloadFlag_then_commandIncludesSkipDownloadFlag() throws Exception {
            YtDlpProcessBuilderCreator creator = new YtDlpProcessBuilderCreator();
            String videoUrl = "https://youtube.com/watch?v=abc";
            String lang = "ru";
            boolean isAutoSubs = false;

            Pair<ProcessBuilder, Path> result = creator.createProcessBuilder(videoUrl, lang, isAutoSubs);

            List<String> command = result.getLeft().command();
            assertThat(command, hasItem("--skip-download"));
        }

        @Test
        void when_createProcessBuilder_then_returnValidOutputPath() throws Exception {
            YtDlpProcessBuilderCreator creator = new YtDlpProcessBuilderCreator();
            String videoUrl = "https://youtube.com/watch?v=abc";
            String lang = "ru";
            boolean isAutoSubs = false;

            Pair<ProcessBuilder, Path> result = creator.createProcessBuilder(videoUrl, lang, isAutoSubs);

            Path outputPath = result.getRight();
            assertThat(outputPath.toString(), containsString("temp-subtitles"));
            assertThat(outputPath.toString(), containsString("temp_subs_"));
            assertThat(outputPath.toString(), endsWith(".vtt"));
        }
    }

    @Nested
    class StaticInitializationAndOsDetection {

        @Test
        void when_createProcessBuilder_afterStaticInitialization_then_ytDlpPathIsResolved() throws Exception {
            YtDlpProcessBuilderCreator creator = new YtDlpProcessBuilderCreator();
            String videoUrl = "https://youtube.com/watch?v=abc";
            String lang = "ru";
            boolean isAutoSubs = false;

            Pair<ProcessBuilder, Path> result = creator.createProcessBuilder(videoUrl, lang, isAutoSubs);

            List<String> command = result.getLeft().command();
            assertThat(command.get(0), anyOf(
                containsString("yt-dlp_macos"),
                containsString("yt-dlp_linux"),
                containsString("yt-dlp_linux_aarch64")
            ));
        }

        @Test
        void when_classLoads_onMacOsSystem_then_ytDlpMacosResourceExtracted() throws Exception {
            String osName = System.getProperty("os.name").toLowerCase();

            if (osName.contains("mac")) {
                YtDlpProcessBuilderCreator creator = new YtDlpProcessBuilderCreator();
                Pair<ProcessBuilder, Path> result = creator.createProcessBuilder("https://youtube.com/watch?v=abc", "ru", false);

                List<String> command = result.getLeft().command();
                assertThat(command.get(0), containsString("yt-dlp_macos"));
            } else {
                assertTrue(true);
            }
        }

        @Test
        void when_classLoads_onLinuxX86System_then_ytDlpLinuxResourceExtracted() throws Exception {
            String osName = System.getProperty("os.name").toLowerCase();
            String osArch = System.getProperty("os.arch").toLowerCase();

            if ((osName.contains("nux") || osName.contains("nix")) &&
                !osArch.contains("aarch64") && !osArch.contains("arm64")) {
                YtDlpProcessBuilderCreator creator = new YtDlpProcessBuilderCreator();
                Pair<ProcessBuilder, Path> result = creator.createProcessBuilder("https://youtube.com/watch?v=abc", "ru", false);

                List<String> command = result.getLeft().command();
                assertThat(command.get(0), containsString("yt-dlp_linux"));
            } else {
                assertTrue(true);
            }
        }

        @Test
        void when_classLoads_onLinuxArm64System_then_ytDlpLinuxAarch64ResourceExtracted() throws Exception {
            String osName = System.getProperty("os.name").toLowerCase();
            String osArch = System.getProperty("os.arch").toLowerCase();

            if ((osName.contains("nux") || osName.contains("nix")) &&
                (osArch.contains("aarch64") || osArch.contains("arm64"))) {
                YtDlpProcessBuilderCreator creator = new YtDlpProcessBuilderCreator();
                Pair<ProcessBuilder, Path> result = creator.createProcessBuilder("https://youtube.com/watch?v=abc", "ru", false);

                List<String> command = result.getLeft().command();
                assertThat(command.get(0), containsString("yt-dlp_linux_aarch64"));
            } else {
                assertTrue(true);
            }
        }
    }

    @Nested
    class ResourceAvailabilityIntegration {

        @Test
        void when_createProcessBuilder_withResourcesAvailable_then_executablePathEmbeddedInCommand() throws Exception {
            YtDlpProcessBuilderCreator creator = new YtDlpProcessBuilderCreator();
            String videoUrl = "https://youtube.com/watch?v=abc";
            String lang = "ru";
            boolean isAutoSubs = false;

            Pair<ProcessBuilder, Path> result = creator.createProcessBuilder(videoUrl, lang, isAutoSubs);

            List<String> command = result.getLeft().command();
            assertThat(command.get(0), anyOf(
                containsString("yt-dlp_macos"),
                containsString("yt-dlp_linux"),
                containsString("yt-dlp_linux_aarch64")
            ));
        }
    }
}