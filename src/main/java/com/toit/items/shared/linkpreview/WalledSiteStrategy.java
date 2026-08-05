package com.toit.items.shared.linkpreview;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 비로그인 접근이 막혀 있어 프리뷰를 얻을 방법이 없는 사이트를 즉시 빈 프리뷰로 끝낸다.
 *
 * <p>이 사이트들은 HTML을 긁어도 로그인 벽({@code /authwall})이나 서비스명만 돌아온다.
 * 그대로 저장하면 제목이 {@code "LinkedIn"}, 썸네일이 링크드인 로고가 되어
 * <b>모든 링크가 똑같이 보인다.</b> 잘못된 값을 저장하는 것보다 비워 두는 편이 낫다.
 * 사용자는 {@code PATCH /links/update}로 직접 제목을 넣을 수 있다.
 *
 * <p>{@link JsoupLinkPreviewStrategy}가 어차피 {@link LinkPreviewQuality}로 걸러내지만,
 * 결과가 뻔한 요청을 미리 끊어 불필요한 외부 호출과 대기 시간을 없앤다.
 *
 * <p><b>인스타그램은 여기 넣지 않았다.</b> 크롤러 User-Agent로 요청하면 og 태그를 주기
 * 때문이다({@link JsoupLinkPreviewStrategy} 참고). 그 경로가 운영에서 통하지 않는다고
 * 확인되면 그때 이 목록으로 옮기면 된다.
 */
@Slf4j
@Order(150)
@Component
public class WalledSiteStrategy implements LinkPreviewStrategy {

    private static final Set<String> WALLED_HOSTS = Set.of(
            "linkedin.com", "www.linkedin.com", "kr.linkedin.com",
            "x.com", "www.x.com",
            "twitter.com", "www.twitter.com", "mobile.twitter.com"
    );

    @Override
    public boolean supports(String url) {
        String host = LinkPreviewStrategy.hostOf(url);
        return host != null && WALLED_HOSTS.contains(host);
    }

    @Override
    public LinkPreview extract(String url) {
        log.info("링크 프리뷰 생략(비로그인 접근 불가 사이트) url={}", url);
        // null이 아니라 빈 프리뷰를 돌려 Jsoup 폴백까지 가지 않게 한다
        return new LinkPreview(url, null, null, null);
    }
}
