package com.toit.items.shared.linkpreview;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 링크 프리뷰 추출 진입점.
 *
 * <p>URL을 정규화한 뒤 {@link LinkPreviewStrategy} 구현체들을 우선순위대로 시도한다.
 * 앞선 전략이 실패하면 다음 전략으로 넘어가고, 최종적으로 {@link JsoupLinkPreviewStrategy}가
 * 폴백으로 동작한다. 모두 실패해도 예외를 던지지 않고 빈 프리뷰를 돌려주므로
 * 링크 저장 자체는 항상 성공한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LinkPreviewExtractor {

    /** Spring이 {@code @Order} 순으로 정렬해 주입한다. */
    private final List<LinkPreviewStrategy> strategies;

    public LinkPreview extract(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return empty(rawUrl);
        }

        String normalizedUrl = normalizeUrl(rawUrl);

        for (LinkPreviewStrategy strategy : strategies) {
            if (!strategy.supports(normalizedUrl)) {
                continue;
            }
            LinkPreview preview = strategy.extract(normalizedUrl);
            if (preview != null) {
                return preview;
            }
            // 실패하면 다음 전략으로 폴백한다
        }

        log.warn("링크 프리뷰 없음(모든 전략 실패) url={}", normalizedUrl);
        return empty(normalizedUrl);
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
}