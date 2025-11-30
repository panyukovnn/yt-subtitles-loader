# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Manifesto

Every time strictly lead rules from manifesto by link: https://raw.githubusercontent.com/panyukovnn/handbook/refs/heads/main/java/java-ai-manifesto.md

## Project Overview

This is a Java library (Spring Boot starter) for loading and processing YouTube video subtitles. It uses the `yt-dlp` command-line tool (bundled as resources) to download subtitles from YouTube videos and provides them in a cleaned, text format.

**Group**: `ru.panyukovnn`
**Java Version**: 17
**Framework**: Spring Boot 3.2.5

## Build Commands

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Run a single test class
./gradlew test --tests "ru.panyukovnn.ytsubtitlesstarter.service.YtSubtitlesLoaderImplUnitTest"

# Run a single test method
./gradlew test --tests "ru.panyukovnn.ytsubtitlesstarter.service.YtSubtitlesLoaderImplUnitTest.testMethodName"

# Clean build
./gradlew clean build

# Publish to GitHub Packages (requires GITHUB_PACKAGES_WRITE_TOKEN env variable)
./gradlew publish -PgithubPackagesWriteToken=$GITHUB_PACKAGES_WRITE_TOKEN
```

## Architecture

### Core Components

**YtSubtitlesLoader** (`src/main/java/ru/panyukovnn/ytsubtitlesstarter/service/YtSubtitlesLoader.java`)
- Main service class for loading subtitles from YouTube videos
- Handles the entire workflow: URL validation → subtitle selection → download → cleaning
- Subtitle priority: Russian manual → Russian auto → English manual → English auto
- Only downloads subtitles in VTT format
- Performs automatic cleanup of temp files older than 15 minutes

**YtDlpProcessBuilderCreator** (`src/main/java/ru/panyukovnn/ytsubtitlesstarter/util/YtDlpProcessBuilderCreator.java`)
- Creates ProcessBuilder instances for running yt-dlp commands
- Extracts platform-specific yt-dlp executable from JAR resources to temp file on first use
- Supports macOS, Linux (x86_64), and Linux (aarch64)
- Handles executable permissions setup for Unix-like systems

**YtLinkHelper** (`src/main/java/ru/panyukovnn/ytsubtitlesstarter/util/YtLinkHelper.java`)
- Validates YouTube URLs
- Cleans YouTube links by removing redundant query parameters

### Data Flow

1. User provides dirty YouTube URL
2. YtSubtitlesLoader validates and cleans the URL (via YtLinkHelper)
3. Calls `yt-dlp --list-subs` to get available subtitles and selects the best option
4. Downloads selected subtitles using `yt-dlp --skip-download --write-subs/--write-auto-subs`
5. Cleans subtitle content (removes VTT tags, timestamps, duplicates)
6. Returns YtSubtitles record with link, language, and cleaned text

### External Dependencies

- **yt-dlp**: Bundled as executable binaries in `src/main/resources/yt-dlp/`
  - `yt-dlp_macos` - macOS binary
  - `yt-dlp_linux` - Linux x86_64 binary
  - `yt-dlp_linux_aarch64` - Linux ARM64 binary
- These are extracted to temp files at runtime and executed as external processes

### Temporary Files

- Subtitle files are downloaded to `./temp-subtitles/` directory
- Files are named: `temp_subs_<timestamp>.<lang>.vtt`
- Files are deleted immediately after reading
- Orphaned files older than 15 minutes are cleaned up automatically

## Testing Strategy

The project uses JUnit 5 (Jupiter) with Spring Boot Test support. Test files are located in `src/test/java/ru/panyukovnn/ytsubtitlesstarter/`:

- Integration tests: `service/YoutubeSubtitlesLoaderTest.java` - Tests with real YouTube videos
- Unit tests: `*UnitTest.java` files - Mock-based tests for individual components

**Important**: Tests require `yt-dlp` to be functional, as the actual executables are used even in tests.

## Configuration Notes

- **bootJar is disabled** (`bootJar.enabled = false`) - this is a library, not an executable application
- Version is determined by git tags via `com.palantir.git-version` plugin
- Publishing is configured for GitHub Packages (repository: `PanyukovNN/reference-logging-starter`)
- JVM arg `-XX:+EnableDynamicAgentLoading` is set for tests (required for Mockito on newer JDKs)

## Code Patterns

- **Exception handling**: Custom `YtLoadingException` with error codes (e.g., "824c", "63e9")
- **Logging**: Uses SLF4J logger with descriptive Russian messages
- **DTOs**: Records are used (YtSubtitles, SubtitlesLang enum)
- **Process execution**: Direct ProcessBuilder usage with stderr/stdout handling
- **Cleanup**: Regex-based subtitle cleaning with LinkedHashSet to preserve order and remove duplicates