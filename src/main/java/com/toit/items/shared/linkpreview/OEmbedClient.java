package com.toit.items.shared.linkpreview;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * oEmbed 엔드포인트를 호출해 프리뷰 값을 얻는다.
 *
 * <p>oEmbed는 사이트가 링크 프리뷰용으로 공개해 둔 공식 창구다. HTML을 긁는 것과 달리
 * 봇 판정 대상이 아니어서, 데이터센터 IP에서 호출해도 정상 응답을 준다.
 *
 * <p>엔드포인트 주소는 HTML {@code <head>}의 자동 발견 링크에서 얻는다.
 * 사이트별로 코드를 넣지 않아도 oEmbed를 지원하는 사이트가 모두 커버된다.
 * <pre>{@code
 * <link rel="alternate" type="application/json+oembed" href="https://.../oembed?url=...">
 * }</pre>
 *
 * @see <a href="https://oembed.com/">oEmbed 스펙 - Discovery</a>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OEmbedClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /**
     * @param endpointUrl 자동 발견 링크에서 얻은 oEmbed 엔드포인트(쿼리 포함)
     * @param pageUrl     프리뷰 대상 페이지의 최종 URL. 응답의 {@code resolvedUrl}로 쓴다.
     * @return 값을 얻지 못하면 {@code null}. 호출자는 다음 수단으로 폴백한다.
     */
    public LinkPreview fetch(String endpointUrl, String pageUrl) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(endpointUrl))
                    .timeout(TIMEOUT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                log.info("oEmbed 응답 없음 endpoint={} status={}", endpointUrl, response.statusCode());
                return null;
            }

            JsonNode json = objectMapper.readTree(response.body());
            String title = text(json, "title");
            String thumbnail = text(json, "thumbnail_url");
            // oEmbed 규격에는 설명 필드가 없다. 프리뷰 카드에 함께 노출되는 제공자·작성자를 대신 쓴다.
            String description = firstNonBlank(text(json, "author_name"), text(json, "provider_name"));

            if (title == null && thumbnail == null) {
                log.info("oEmbed 내용 없음 endpoint={}", endpointUrl);
                return null;
            }

            log.info("oEmbed 자동 발견 성공 pageUrl={} endpoint={} title={}", pageUrl, endpointUrl, title);
            return new LinkPreview(
                    pageUrl,
                    LinkPreviewStrategy.sanitizeTitle(title),
                    LinkPreviewStrategy.sanitizeDescription(description),
                    LinkPreviewStrategy.sanitizeUrl(thumbnail));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("oEmbed 중단 endpoint={}", endpointUrl);
            return null;
        } catch (Exception e) {
            log.warn("oEmbed 실패 endpoint={} cause={}: {}",
                    endpointUrl, e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    private static String text(JsonNode json, String field) {
        JsonNode node = json.get(field);
        if (node == null || !node.isTextual()) {
            return null;
        }
        String value = node.asText().trim();
        return value.isEmpty() ? null : value;
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }
}
