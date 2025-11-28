package ru.panyukovnn.ytsubtitlesstarter.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.panyukovnn.ytsubtitlesstarter.exception.YtLoadingException;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Pattern;

@Slf4j
@Service
public class YtLinkHelper {

    public static final Pattern YOUTUBE_VIDEO_ID_PATTERN = Pattern.compile("^[\\w-]{11}$");

    public String removeRedundantQueryParamsFromYoutubeLint(String youtubeLink) {
        try {
            URI uri = new URI(youtubeLink);
            String query = uri.getRawQuery();

            if (query == null || query.isEmpty()) {
                return youtubeLink;
            }

            String vValue = null;
            for (String param : query.split("&")) {
                String[] pair = param.split("=", 2);
                if (pair.length == 2 && pair[0].equals("v")) {
                    vValue = pair[1];
                    break;
                }
            }

            URI cleanedUri = new URI(
                uri.getScheme(),
                uri.getAuthority(),
                uri.getPath(),
                vValue != null ? "v=" + vValue : null,
                uri.getFragment()
            );

            return cleanedUri.toString();
        } catch (URISyntaxException e) {
            throw new YtLoadingException("4bc5", "Невалидная ссылка youtube", e);
        }
    }

    public boolean isValidYoutubeUrl(String url) {
        try {
            URL parsedUrl = new URI(url).toURL();
            String host = parsedUrl.getHost().toLowerCase();
            String path = parsedUrl.getPath();
            String query = parsedUrl.getQuery();

            if (!isValidHostSyntax(host)) {
                return false;
            }

            if (isValidYouTubeHost(host)) {
                if (host.equals("youtu.be")) {
                    String id = path.replaceFirst("^/", "");
                    return YOUTUBE_VIDEO_ID_PATTERN.matcher(id).matches();
                }

                if (path.startsWith("/watch")) {
                    if (query == null) return false;
                    for (String param : query.split("&")) {
                        if (param.startsWith("v=")) {
                            String id = param.substring(2);
                            return YOUTUBE_VIDEO_ID_PATTERN.matcher(id).matches();
                        }
                    }
                } else if (path.startsWith("/shorts/") || path.startsWith("/live/")) {
                    String[] segments = path.split("/");
                    if (segments.length >= 3) {
                        String id = segments[2];
                        return YOUTUBE_VIDEO_ID_PATTERN.matcher(id).matches();
                    }
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);

            return false;
        }

        return false;
    }

    private boolean isValidHostSyntax(String host) {
        if (host == null || host.isEmpty()) return false;
        if (host.startsWith(".")) return false;
        if (!host.matches("^[a-z0-9.-]+$")) return false;

        return true;
    }

    private boolean isValidYouTubeHost(String host) {
        // Явные валидные хосты
        if (host.equals("youtube.com") || host.equals("youtu.be")) {
            return true;
        }

        // Поддомены youtube.com, но не что-то вроде ".youtube.com" или "fakeyoutube.com"
        if (host.endsWith(".youtube.com")) {
            // Отсекаем всё до последней точки перед youtube.com
            String[] parts = host.split("\\.");
            int len = parts.length;
            if (len >= 3 && parts[len - 2].equals("youtube") && parts[len - 1].equals("com")) {
                return true;
            }
        }

        return false;
    }
}
