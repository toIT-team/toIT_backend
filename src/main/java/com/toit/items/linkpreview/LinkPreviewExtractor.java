package com.toit.items.linkpreview;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Duration;

@Component
public class LinkPreviewExtractor {

    // 운영에서 적당한 값으로 조절 (너무 길면 요청 스레드 묶임)
    private static final int TIMEOUT_MS = (int) Duration.ofSeconds(5).toMillis();

    public LinkPreview extract(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return empty(rawUrl);
        }

        String normalizedUrl = normalizeUrl(rawUrl);

        try {
            Document doc = Jsoup.connect(normalizedUrl)
                    .userAgent("Mozilla/5.0 (compatible; toITLinkBot/1.0)")
                    .referrer("https://www.google.com")
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true)
                    .get();

            // 최종 URL(리다이렉트 반영)
            String resolvedUrl = doc.location();

            String title = firstNonBlank(
                    meta(doc, "property", "og:title"),
                    meta(doc, "name", "twitter:title"),
                    doc.title()
            );

            String description = firstNonBlank(
                    meta(doc, "property", "og:description"),
                    meta(doc, "name", "twitter:description"),
                    meta(doc, "name", "description")
            );

            String thumbnail = firstNonBlank(
                    meta(doc, "property", "og:image"),
                    meta(doc, "name", "twitter:image"),
                    meta(doc, "property", "twitter:image")
            );

            // og:image가 상대경로인 경우 absUrl로 보정
            if (thumbnail != null && !thumbnail.isBlank()) {
                // meta로 가져온 thumbnail은 content 문자열이라 absUrl이 안 먹음 → 직접 보정
                thumbnail = absolutize(resolvedUrl, thumbnail);
            } else {
                // fallback: favicon
                Element icon = doc.selectFirst("link[rel~=(?i)^(shortcut )?icon$]");
                if (icon != null) {
                    String faviconHref = icon.hasAttr("href") ? icon.attr("href") : "";
                    String abs = icon.absUrl("href");
                    thumbnail = (abs != null && !abs.isBlank())
                            ? abs
                            : absolutize(resolvedUrl, faviconHref);
                }
            }
            return new LinkPreview(resolvedUrl, title, sanitize(description), sanitize(thumbnail));

        } catch (Exception e) {
            // 운영에서는 logger로 남기고, 서비스는 fallback
            return empty(normalizedUrl);
        }
    }

    private static String meta(Document doc, String attrKey, String attrValue) {
        Element el = doc.selectFirst("meta[" + attrKey + "=" + attrValue + "]");
        if (el == null) return null;
        String content = el.attr("content");
        return (content == null || content.isBlank()) ? null : content.trim();
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v.trim();
        }
        return null;
    }

    private static LinkPreview empty(String url) {
        return new LinkPreview(url, null, null, null);
    }

    private static String normalizeUrl(String rawUrl) {
        String u = rawUrl.trim();
        // 사용자가 www.example.com만 넣는 경우 대비
        if (!u.startsWith("http://") && !u.startsWith("https://")) {
            u = "https://" + u;
        }
        return u;
    }

    private static String absolutize(String baseUrl, String maybeRelative) {
        if (maybeRelative == null || maybeRelative.isBlank()) return null;
        String v = maybeRelative.trim();
        // 이미 절대경로면 그대로
        if (v.startsWith("http://") || v.startsWith("https://")) return v;

        try {
            URI base = URI.create(baseUrl);
            return base.resolve(v).toString();
        } catch (Exception e) {
            return v; // 최후의 fallback
        }
    }

    private static String sanitize(String s) {
        if (s == null) return null;
        // 길이 제한(DB 컬럼 길이에 맞춰 조절)
        s = s.strip();
        if (s.isBlank()) return null;
        return s.length() > 500 ? s.substring(0, 500) : s;
    }
}