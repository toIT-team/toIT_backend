package com.toit.items.shared.linkpreview;

import java.util.Set;

/**
 * 추출한 프리뷰가 실제로 쓸만한지 판정한다.
 *
 * <p>링크 프리뷰의 실패는 예외로 드러나지 않는다. 로그인 벽이나 봇 판정에 걸린 페이지도
 * HTTP 200으로 내려오기 때문에, 폴백 로직이 끝까지 내려가 사이트 전체를 설명하는 값
 * (예: {@code "Instagram"}, {@code "- YouTube"})을 제목으로 잡아 버린다.
 * 그런 값은 저장하는 것보다 비워 두는 편이 낫다. 사용자가 직접 제목을 수정할 수 있기 때문이다.
 *
 * <p>주의: <b>og 태그가 없다는 것 자체는 실패가 아니다.</b> velog처럼 og 태그 없이
 * {@code <title>}과 {@code meta[name=description]}만 제공하는 정상 사이트가 많다.
 * 그래서 폴백 단계를 보지 않고 <b>결과값이 쓰레기인지만</b> 판정한다.
 */
public final class LinkPreviewQuality {

    /** 판정 결과. OK가 아니면 실패이며, 값 자체가 로그에 남길 사유가 된다. */
    public enum Verdict {
        OK,
        /** 제목을 아무것도 찾지 못했다. */
        BLANK_TITLE,
        /** 리다이렉트를 따라간 결과가 로그인·동의 페이지였다. */
        AUTH_WALL,
        /** 제목이 서비스명이나 차단 안내문뿐이었다. */
        JUNK_TITLE;

        public boolean usable() {
            return this == OK;
        }
    }

    /**
     * 최종 URL에 이 조각이 들어 있으면 원래 보려던 페이지를 보지 못한 것으로 본다.
     * 오탐이 거의 없어 가장 신뢰도가 높은 신호다.
     */
    private static final Set<String> AUTH_WALL_MARKERS = Set.of(
            "/accounts/login",   // 인스타그램
            "/authwall",         // 링크드인
            "/login",
            "/signin",
            "/sign_in",
            "/oauth/authorize",
            "consent."           // 구글 계열 동의 페이지
    );

    /**
     * 제목이 이 값과 <b>정확히 일치</b>할 때만 걸러낸다.
     * {@code contains}로 비교하면 "Instagram 마케팅 완전정복" 같은 정상 제목까지 막힌다.
     *
     * <p>미리 완성할 수 없는 목록이다. 품질 미달 로그를 보고 계속 늘려야 한다.
     */
    private static final Set<String> JUNK_TITLES = Set.of(
            // 서비스명만 돌아온 경우
            "instagram", "linkedin", "facebook", "threads", "youtube", "- youtube",
            "x", "twitter", "tiktok",
            // 로그인 요구
            "log in", "login", "log in or sign up", "sign in", "sign up",
            "로그인", "로그인 - ", "로그인하세요",
            // 봇 차단·챌린지 페이지
            "just a moment...", "attention required!", "access denied", "forbidden",
            "are you a robot?", "verify you are human", "security check",
            "잠시만 기다려주세요", "접근이 거부되었습니다",
            // 빈 껍데기
            "error", "not found", "404 not found", "페이지를 찾을 수 없습니다"
    );

    private LinkPreviewQuality() {
    }

    /**
     * @param title       추출한 제목
     * @param resolvedUrl 리다이렉트를 모두 따라간 최종 URL
     */
    public static Verdict judge(String title, String resolvedUrl) {
        if (title == null || title.isBlank()) {
            return Verdict.BLANK_TITLE;
        }
        if (isAuthWall(resolvedUrl)) {
            return Verdict.AUTH_WALL;
        }
        if (isJunkTitle(title)) {
            return Verdict.JUNK_TITLE;
        }
        return Verdict.OK;
    }

    private static boolean isAuthWall(String resolvedUrl) {
        if (resolvedUrl == null || resolvedUrl.isBlank()) {
            return false;
        }
        String lower = resolvedUrl.toLowerCase();
        return AUTH_WALL_MARKERS.stream().anyMatch(lower::contains);
    }

    private static boolean isJunkTitle(String title) {
        return JUNK_TITLES.contains(title.strip().toLowerCase());
    }
}
