package com.toit.items.shared.linkpreview;

import java.net.URI;

/**
 * 링크 프리뷰 추출 전략.
 *
 * <p>사이트마다 프리뷰를 얻는 방법이 다르다. 대부분은 HTML의 og 태그를 읽으면 되지만,
 * 유튜브처럼 데이터센터 IP에서 온 요청에는 메타데이터를 뺀 HTML을 내려주는 곳이 있어
 * 해당 사이트가 공식 제공하는 API를 따로 호출해야 한다.
 *
 * <p>전략은 {@link org.springframework.core.annotation.Order} 순으로 시도되며,
 * 마지막에 항상 {@link JsoupLinkPreviewStrategy}가 폴백으로 동작한다.
 * 새 사이트 대응이 필요하면 이 인터페이스 구현체를 추가하기만 하면 된다.
 */
public interface LinkPreviewStrategy {

    /** 이 전략이 처리할 수 있는 URL인지 판단한다. */
    boolean supports(String url);

    /**
     * 프리뷰를 추출한다.
     *
     * @return 추출 실패 시 {@code null}. 호출자는 다음 전략으로 폴백한다.
     */
    LinkPreview extract(String url);

    /** URL에서 호스트를 꺼낸다. 파싱할 수 없는 URL이면 {@code null}. */
    static String hostOf(String url) {
        try {
            String host = URI.create(url).getHost();
            return (host == null) ? null : host.toLowerCase();
        } catch (Exception e) {
            return null;
        }
    }

    /** {@code Links.linksName} 컬럼 길이 */
    int TITLE_MAX = 255;

    /** {@code ItemsBase.textContent} 컬럼 길이 */
    int DESCRIPTION_MAX = 1000;

    /** {@code Links.linksThumbnail} 컬럼 길이 */
    int URL_MAX = 2000;

    /** 제목을 컬럼 길이에 맞춘다. 넘치면 잘라도 의미가 보존된다. */
    static String sanitizeTitle(String title) {
        return truncate(title, TITLE_MAX);
    }

    /** 설명을 컬럼 길이에 맞춘다. 넘치면 잘라도 의미가 보존된다. */
    static String sanitizeDescription(String description) {
        return truncate(description, DESCRIPTION_MAX);
    }

    /**
     * URL은 <b>절대 중간에서 자르지 않는다.</b>
     *
     * <p>인스타그램 CDN 같은 서명된 URL은 뒤쪽에 서명({@code oh})과 만료 시각({@code oe})이
     * 붙어 500자를 넘는다(실측 557자). 잘라내면 이미지가 아예 안 뜨므로,
     * <b>잘린 URL을 저장하는 것보다 비워 두는 편이 낫다.</b>
     *
     * @return 컬럼 길이를 넘으면 {@code null}
     */
    static String sanitizeUrl(String url) {
        if (url == null) return null;
        String trimmed = url.strip();
        if (trimmed.isBlank()) return null;
        return (trimmed.length() > URL_MAX) ? null : trimmed;
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        String trimmed = s.strip();
        if (trimmed.isBlank()) return null;
        return (trimmed.length() > max) ? trimmed.substring(0, max) : trimmed;
    }
}