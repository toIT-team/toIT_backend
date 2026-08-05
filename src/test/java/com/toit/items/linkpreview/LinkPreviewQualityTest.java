package com.toit.items.linkpreview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.toit.items.shared.linkpreview.LinkPreviewQuality;
import com.toit.items.shared.linkpreview.LinkPreviewQuality.Verdict;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LinkPreviewQualityTest {

    private static final String NORMAL_URL = "https://velog.io/@velopert/react-hooks";

    @Test
    @DisplayName("정상적인 제목은 통과한다")
    void judge_shouldPass_whenTitleIsMeaningful() {
        Verdict verdict = LinkPreviewQuality.judge("리액트의 Hooks 완벽 정복하기", NORMAL_URL);

        assertEquals(Verdict.OK, verdict);
        assertTrue(verdict.usable());
    }

    @Test
    @DisplayName("제목이 없으면 BLANK_TITLE로 판정한다")
    void judge_shouldReturnBlankTitle_whenTitleIsMissing() {
        assertEquals(Verdict.BLANK_TITLE, LinkPreviewQuality.judge(null, NORMAL_URL));
        assertEquals(Verdict.BLANK_TITLE, LinkPreviewQuality.judge("   ", NORMAL_URL));
    }

    @Test
    @DisplayName("리다이렉트 결과가 로그인 페이지면 AUTH_WALL로 판정한다")
    void judge_shouldReturnAuthWall_whenRedirectedToLoginPage() {
        Verdict instagram = LinkPreviewQuality.judge(
                "로그인 • Instagram",
                "https://www.instagram.com/accounts/login/?next=%2Fp%2FABC123%2F");
        Verdict linkedIn = LinkPreviewQuality.judge(
                "회원가입 | LinkedIn",
                "https://www.linkedin.com/authwall?trk=bf&originalReferer=");

        assertEquals(Verdict.AUTH_WALL, instagram);
        assertEquals(Verdict.AUTH_WALL, linkedIn);
    }

    @Test
    @DisplayName("제목이 서비스명뿐이면 JUNK_TITLE로 판정한다")
    void judge_shouldReturnJunkTitle_whenTitleIsOnlyServiceName() {
        // 인스타그램이 봇 판정 시 내려주는 껍데기 페이지의 <title>
        assertEquals(Verdict.JUNK_TITLE, LinkPreviewQuality.judge("Instagram", NORMAL_URL));
        // 유튜브 HTML을 데이터센터 IP에서 긁었을 때 잡히는 값
        assertEquals(Verdict.JUNK_TITLE, LinkPreviewQuality.judge("- YouTube", NORMAL_URL));
    }

    @Test
    @DisplayName("Cloudflare 챌린지 페이지의 제목을 걸러낸다")
    void judge_shouldReturnJunkTitle_whenTitleIsBotChallengePage() {
        assertEquals(Verdict.JUNK_TITLE, LinkPreviewQuality.judge("Just a moment...", NORMAL_URL));
        assertEquals(Verdict.JUNK_TITLE, LinkPreviewQuality.judge("Attention Required!", NORMAL_URL));
    }

    @Test
    @DisplayName("대소문자와 앞뒤 공백에 관계없이 걸러낸다")
    void judge_shouldReturnJunkTitle_regardlessOfCaseAndWhitespace() {
        assertEquals(Verdict.JUNK_TITLE, LinkPreviewQuality.judge("  INSTAGRAM  ", NORMAL_URL));
        assertEquals(Verdict.JUNK_TITLE, LinkPreviewQuality.judge("Just A Moment...", NORMAL_URL));
    }

    @Test
    @DisplayName("서비스명이 포함되기만 한 정상 제목은 통과한다")
    void judge_shouldPass_whenTitleMerelyContainsServiceName() {
        // contains로 비교하면 이런 정상 제목까지 막힌다. 정확히 일치할 때만 걸러야 한다.
        assertTrue(LinkPreviewQuality.judge("Instagram 마케팅 완전정복", NORMAL_URL).usable());
        assertTrue(LinkPreviewQuality.judge("YouTube 알고리즘 분석", NORMAL_URL).usable());
    }

    @Test
    @DisplayName("og 태그 없이 <title>로 폴백한 것만으로는 실패로 보지 않는다")
    void judge_shouldPass_whenSiteHasNoOgTagsButValidTitle() {
        // velog는 og 태그가 없고 <title>과 meta[name=description]만 제공하는 정상 사이트다.
        // 폴백 단계를 실패 근거로 쓰면 이런 사이트가 함께 막힌다.
        Verdict verdict = LinkPreviewQuality.judge("리액트의 Hooks 완벽 정복하기", NORMAL_URL);

        assertTrue(verdict.usable());
    }

    @Test
    @DisplayName("최종 URL이 없어도 제목만으로 판정한다")
    void judge_shouldNotFail_whenResolvedUrlIsMissing() {
        assertTrue(LinkPreviewQuality.judge("정상 제목", null).usable());
        assertFalse(LinkPreviewQuality.judge("Instagram", null).usable());
    }
}
