package com.toit.items.shared.linkpreview;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;

/**
 * 유튜브 링크 프리뷰 - 공식 oEmbed API 사용.
 *
 * <p>유튜브는 데이터센터(AWS 등) IP에서 크롤링하면 200을 주면서도 제목/썸네일이 빠진
 * 껍데기 HTML을 내려준다. User-Agent를 브라우저로 위장해도 소용없는데, IP 대역으로
 * 판별하기 때문이다. 반면 oEmbed는 링크 프리뷰용으로 공개된 공식 창구라
 * 서버에서 호출해도 정상적으로 메타데이터를 준다.
 *
 * <p>watch / youtu.be / shorts / m.youtube 등 URL 형태를 엔드포인트가 모두 처리하므로
 * 영상 ID를 직접 파싱하지 않고 URL을 그대로 넘긴다.
 */
@Slf4j
@Order(100)
@Component
@RequiredArgsConstructor
public class YouTubeOEmbedStrategy implements LinkPreviewStrategy {

    private static final Set<String> YOUTUBE_HOSTS = Set.of(
            "youtube.com", "www.youtube.com", "m.youtube.com",
            "music.youtube.com", "youtu.be", "www.youtu.be"
    );

    private static final String OEMBED_ENDPOINT = "https://www.youtube.com/oembed";
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Override
    public boolean supports(String url) {
        String host = LinkPreviewStrategy.hostOf(url);
        return host != null && YOUTUBE_HOSTS.contains(host);
    }

    @Override
    public LinkPreview extract(String url) {
        String requestUrl = OEMBED_ENDPOINT
                + "?url=" + URLEncoder.encode(url, StandardCharsets.UTF_8)
                + "&format=json";

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(requestUrl))
                    .timeout(TIMEOUT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                // 비공개/삭제된 영상이거나 영상 URL이 아니면 400, 없는 영상이면 404
                log.info("유튜브 oEmbed 응답 없음 url={} status={}", url, response.statusCode());
                return emptyPreview(url);
            }

            JsonNode json = objectMapper.readTree(response.body());
            String title = text(json, "title");
            String thumbnail = text(json, "thumbnail_url");
            // oEmbed에는 영상 설명이 없다. 프리뷰 카드에 보통 함께 노출되는 채널명을 대신 쓴다.
            String author = text(json, "author_name");

            if (title == null && thumbnail == null) {
                log.info("유튜브 oEmbed 내용 없음 url={}", url);
                return emptyPreview(url);
            }

            log.info("유튜브 oEmbed 성공 url={} title={}", url, title);
            return new LinkPreview(
                    url,
                    LinkPreviewStrategy.sanitizeTitle(title),
                    LinkPreviewStrategy.sanitizeDescription(author),
                    LinkPreviewStrategy.sanitizeUrl(thumbnail));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("유튜브 oEmbed 중단 url={}", url);
            return null;
        } catch (Exception e) {
            log.warn("유튜브 oEmbed 실패 url={} cause={}: {}",
                    url, e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    /**
     * 유튜브 URL인데 oEmbed로 값을 못 얻은 경우 Jsoup 폴백으로 넘기지 않고 빈 프리뷰로 끝낸다.
     * 유튜브 HTML은 크롤링해 봐야 제목이 {@code "- YouTube"}, 설명은 유튜브 홈 소개문,
     * 썸네일은 파비콘으로 잡혀 오히려 잘못된 값이 저장되기 때문이다.
     */
    private static LinkPreview emptyPreview(String url) {
        return new LinkPreview(url, null, null, null);
    }

    private static String text(JsonNode json, String field) {
        JsonNode node = json.get(field);
        if (node == null || !node.isTextual()) {
            return null;
        }
        String value = node.asText().trim();
        return value.isEmpty() ? null : value;
    }
}
