package com.toit.items.shared.linkpreview;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Duration;
import java.util.List;

/**
 * 기본 전략 - HTML을 직접 받아 og 태그를 읽는다.
 *
 * <p>대부분의 사이트는 og 태그를 그대로 내려주므로 이 전략으로 충분하다.
 * 어떤 전략도 처리하지 못한 URL을 받는 최종 폴백이라 {@link #supports}는 항상 참이다.
 *
 * <p>og 태그를 얻지 못하면 두 가지를 더 시도한다.
 * <ol>
 *   <li>{@code <head>}의 oEmbed 자동 발견 링크 → {@link OEmbedClient}</li>
 *   <li>다른 User-Agent로 재요청 ({@link #USER_AGENTS})</li>
 * </ol>
 *
 * <p>마지막으로 {@link LinkPreviewQuality}로 결과를 검증한다. 로그인 벽이나 봇 판정에 걸린
 * 페이지도 200으로 내려오기 때문에, 검증 없이 반환하면 {@code "Instagram"} 같은 값이 저장된다.
 */
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
@Component
@RequiredArgsConstructor
public class JsoupLinkPreviewStrategy implements LinkPreviewStrategy {

    /**
     * 요청 스레드가 묶이므로 짧게 잡는다. User-Agent를 바꿔 최대 2번 시도하므로
     * 최악의 경우 이 값의 2배까지 걸린다.
     */
    private static final int TIMEOUT_MS = (int) Duration.ofSeconds(4).toMillis();

    /** og 태그는 {@code <head>}에 있어 기본값(2MB)으로도 충분하지만, 무거운 페이지에서 body가 잘리는 것을 방지 */
    private static final int MAX_BODY_SIZE = 3 * 1024 * 1024;

    /**
     * 시도 순서대로 나열한 User-Agent.
     *
     * <p><b>1) 크롤러 UA를 먼저 쓴다.</b> og 태그는 애초에 링크 프리뷰 크롤러를 위해 만들어진
     * 규격이라, 알려진 프리뷰 크롤러에게만 og 태그를 내려주는 사이트가 있다. 인스타그램이
     * 그렇다. 같은 URL에 UA만 바꿔 요청하면 결과가 갈린다(로컬 측정).
     * <pre>
     *   facebookexternalhit/1.1                → og 태그 6개
     *   Twitterbot/1.0                         → og 태그 6개
     *   Mozilla/5.0 ... Chrome/126             → og 태그 0개
     * </pre>
     *
     * <p><b>2) 브라우저 UA로 폴백한다.</b> 반대로 Cloudflare·Akamai 같은 WAF 뒤에 있는
     * 사이트는 UA에 {@code bot}이 들어가면 403이나 챌린지 페이지를 준다. 어느 한쪽으로
     * 고정하면 반대쪽을 놓치므로 둘 다 시도한다. 대다수 사이트는 UA와 무관하게 og 태그를
     * 주므로 1차에서 끝나고 요청은 한 번만 나간다.
     *
     * <p><b>남겨둘 점.</b> 첫 번째 값은 실제로는 Meta 크롤러가 아니므로 사칭이다. 규범에 맞는
     * 방식은 연락처를 담은 자체 UA({@code toITLinkBot/1.0 (+https://.../bot)})지만, 화이트리스트
     * 방식인 사이트에서는 알려지지 않은 UA가 우대받지 못해 효과가 없다. 또한 Meta가 요청 IP가
     * 실제 Meta 대역인지 검증을 추가하면 그날부터 통하지 않는다. 영구적 해결책이 아니라
     * <b>지금 통하는 방법</b>으로 보고, 실패 시 조용히 폴백하도록 구성했다.
     */
    private static final List<String> USER_AGENTS = List.of(
            "facebookexternalhit/1.1 (+http://www.facebook.com/externalhit_uatext.php)",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
    );

    /**
     * Accept-Language를 보내지 않으면 상대 서버가 요청 IP의 지역으로 언어를 추측한다.
     * 그 결과 로컬(KR)과 배포 리전에서 서로 다른 언어의 제목/설명이 내려올 수 있어 한국어로 고정한다.
     */
    private static final String ACCEPT_LANGUAGE = "ko-KR,ko;q=0.9,en;q=0.8";

    private static final String ACCEPT =
            "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";

    /** oEmbed 자동 발견 링크. @see <a href="https://oembed.com/">oEmbed 스펙</a> */
    private static final String OEMBED_DISCOVERY_SELECTOR =
            "link[type=\"application/json+oembed\"]";

    private final OEmbedClient oEmbedClient;

    @Override
    public boolean supports(String url) {
        return true; // 최종 폴백
    }

    @Override
    public LinkPreview extract(String url) {
        for (int i = 0; i < USER_AGENTS.size(); i++) {
            boolean lastAttempt = (i == USER_AGENTS.size() - 1);
            Attempt attempt = tryExtract(url, USER_AGENTS.get(i), lastAttempt);

            if (attempt.preview() != null) {
                return attempt.preview();
            }
            if (!attempt.worthRetrying()) {
                return null;
            }
        }
        return null;
    }

    /**
     * 한 개의 User-Agent로 1회 시도한다.
     *
     * @param lastAttempt 마지막 시도라면 실패를 {@code warn}으로 남긴다. 중간 시도는 재시도할
     *                    것이므로 로그를 시끄럽게 만들지 않는다.
     */
    private Attempt tryExtract(String url, String userAgent, boolean lastAttempt) {
        try {
            Connection.Response response = Jsoup.connect(url)
                    .userAgent(userAgent)
                    .header("Accept-Language", ACCEPT_LANGUAGE)
                    .header("Accept", ACCEPT)
                    .referrer("https://www.google.com")
                    .timeout(TIMEOUT_MS)
                    .maxBodySize(MAX_BODY_SIZE)
                    .followRedirects(true)
                    // 4xx/5xx를 예외로 던지지 않고 응답으로 받아, 차단인지 아닌지 로그로 구분한다
                    .ignoreHttpErrors(true)
                    .execute();

            // 최종 URL(리다이렉트 반영)
            String resolvedUrl = response.url().toString();

            if (response.statusCode() >= 400) {
                // WAF가 봇 UA를 막은 경우일 수 있어 다른 UA로 재시도할 가치가 있다
                log(lastAttempt, "링크 프리뷰 실패(오류 응답) url={} status={} finalUrl={} ua={}",
                        url, response.statusCode(), resolvedUrl, shortUa(userAgent));
                return Attempt.retry();
            }

            if (!isHtml(response.contentType())) {
                // PDF/이미지 등은 UA를 바꿔도 결과가 같다
                log.info("링크 프리뷰 건너뜀(HTML 아님) url={} contentType={}",
                        url, response.contentType());
                return Attempt.giveUp();
            }

            Document doc = response.parse();

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

            // og 태그를 못 얻었으면 이 페이지가 oEmbed를 제공하는지 확인한다.
            // oEmbed는 봇 판정 대상이 아니라, HTML에서 태그가 빠져도 값이 오는 경우가 있다.
            if (isBlank(title) || isBlank(thumbnail)) {
                LinkPreview viaOEmbed = tryOEmbedDiscovery(doc, resolvedUrl);
                if (viaOEmbed != null
                        && LinkPreviewQuality.judge(viaOEmbed.getTitle(), resolvedUrl).usable()) {
                    return Attempt.success(viaOEmbed);
                }
            }

            if (isBlank(thumbnail)) {
                // fallback: favicon (프리뷰 실패에 가까운 상태이므로 제목 검증을 통과해야 저장된다)
                Element icon = doc.selectFirst("link[rel~=(?i)^(shortcut )?icon$]");
                if (icon != null) {
                    String faviconHref = icon.hasAttr("href") ? icon.attr("href") : "";
                    String abs = icon.absUrl("href");
                    thumbnail = !isBlank(abs) ? abs : absolutize(resolvedUrl, faviconHref);
                }
            } else {
                // meta로 가져온 thumbnail은 content 문자열이라 absUrl이 안 먹음 → 직접 보정
                thumbnail = absolutize(resolvedUrl, thumbnail);
            }

            // 200이어도 로그인 벽·차단 페이지를 긁었을 수 있다. 여기서 걸러내지 않으면
            // 제목이 "Instagram", "- YouTube" 같은 값으로 저장된다.
            LinkPreviewQuality.Verdict verdict = LinkPreviewQuality.judge(title, resolvedUrl);
            if (!verdict.usable()) {
                log(lastAttempt, "링크 프리뷰 품질 미달 url={} finalUrl={} title={} 사유={} ua={}",
                        url, resolvedUrl, title, verdict, shortUa(userAgent));
                return Attempt.retry();
            }

            log.info("링크 프리뷰 성공 url={} finalUrl={} title={} ua={}",
                    url, resolvedUrl, title, shortUa(userAgent));

            return Attempt.success(new LinkPreview(
                    resolvedUrl,
                    LinkPreviewStrategy.sanitizeTitle(title),
                    LinkPreviewStrategy.sanitizeDescription(description),
                    LinkPreviewStrategy.sanitizeUrl(thumbnail)));

        } catch (Exception e) {
            // 타임아웃·연결 실패는 UA를 바꿔도 마찬가지이고, 재시도하면 대기 시간만 2배가 된다
            log.warn("링크 프리뷰 실패 url={} ua={} cause={}: {}",
                    url, shortUa(userAgent), e.getClass().getSimpleName(), e.getMessage());
            return Attempt.giveUp();
        }
    }

    /** {@code <head>}에 oEmbed 자동 발견 링크가 있으면 호출한다. */
    private LinkPreview tryOEmbedDiscovery(Document doc, String resolvedUrl) {
        Element link = doc.selectFirst(OEMBED_DISCOVERY_SELECTOR);
        if (link == null) {
            return null;
        }
        String endpoint = link.absUrl("href");
        if (isBlank(endpoint)) {
            return null;
        }
        return oEmbedClient.fetch(endpoint, resolvedUrl);
    }

    /** 마지막 시도의 실패만 {@code warn}으로 남긴다. 중간 실패는 재시도하므로 {@code info}. */
    private static void log(boolean lastAttempt, String message, Object... args) {
        if (lastAttempt) {
            log.warn(message, args);
        } else {
            log.info(message, args);
        }
    }

    /** 로그에 UA 전체를 남기면 한 줄이 너무 길어져 앞부분만 남긴다. */
    private static String shortUa(String userAgent) {
        int cut = userAgent.indexOf(' ');
        return (cut > 0) ? userAgent.substring(0, cut) : userAgent;
    }

    /** Content-Type이 HTML 계열일 때만 파싱한다. (PDF/이미지 링크 등은 프리뷰 대상이 아님) */
    private static boolean isHtml(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return true; // 헤더가 없으면 일단 시도
        }
        String ct = contentType.toLowerCase();
        return ct.startsWith("text/html") || ct.startsWith("application/xhtml");
    }

    private static String meta(Document doc, String attrKey, String attrValue) {
        Element el = doc.selectFirst("meta[" + attrKey + "=" + attrValue + "]");
        if (el == null) return null;
        String content = el.attr("content");
        return isBlank(content) ? null : content.trim();
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (!isBlank(v)) return v.trim();
        }
        return null;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String absolutize(String baseUrl, String maybeRelative) {
        if (isBlank(maybeRelative)) return null;
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

    /**
     * 한 번의 시도 결과.
     *
     * @param preview       성공한 프리뷰. 실패면 {@code null}
     * @param worthRetrying 다른 User-Agent로 재시도할 가치가 있는지
     */
    private record Attempt(LinkPreview preview, boolean worthRetrying) {

        static Attempt success(LinkPreview preview) {
            return new Attempt(preview, false);
        }

        /** 차단이나 품질 미달 — UA를 바꾸면 결과가 달라질 수 있다. */
        static Attempt retry() {
            return new Attempt(null, true);
        }

        /** UA와 무관한 실패 — 재시도해도 같거나 대기 시간만 늘어난다. */
        static Attempt giveUp() {
            return new Attempt(null, false);
        }
    }
}
