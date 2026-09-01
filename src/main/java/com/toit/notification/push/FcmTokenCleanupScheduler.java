package com.toit.notification.push;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * 오래 안 쓴 기기 토큰을 정리한다.
 *
 * 죽은 토큰은 보내봐야 알 수 있다. UNREGISTERED 를 받으면 그 자리에서 지우지만,
 * 그건 발송을 시도했을 때뿐이다. 로그아웃한 기기나 한동안 안 쓰는 사용자의
 * 토큰은 아무도 알려주지 않아 계속 남고, 남으면 발송마다 실패 왕복이 붙는다.
 *
 * 기간은 60일로 둔다. FCM 이 Android 토큰을 만료로 보는 270일보다는 훨씬 짧고,
 * 두 달쯤 안 들어온 기기라면 다시 열 때 토큰을 새로 등록하므로 지워도 잃는 게 없다.
 * 반대로 너무 짧게 잡으면 잠깐 안 쓴 사용자의 토큰까지 지워, 앱을 다시 열기 전까지
 * 알림이 끊긴다.
 * https://firebase.google.com/docs/cloud-messaging/manage-tokens?hl=ko
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FcmTokenCleanupScheduler {

    /** 이만큼 갱신이 없으면 안 쓰는 기기로 본다. */
    private static final long STALE_DAYS = 60;

    private final FcmTokenRepository fcmTokenRepository;
    private final Clock clock;

    // @Scheduled 는 기본이 단일 스레드라 알림 스케줄러와 같은 줄을 선다.
    // 알림은 매분 0초에 뜨므로 30초로 비켜 둔다.
    @Scheduled(cron = "30 0 4 * * *")
    @Transactional
    public void pruneStaleTokens() {
        LocalDateTime threshold = LocalDateTime.now(clock).minusDays(STALE_DAYS);
        int deleted = fcmTokenRepository.deleteByLastUpdatedAtBefore(threshold);

        // 지울 게 없는 날이 대부분이라 0 이면 조용히 넘어간다.
        if (deleted > 0) {
            log.info("[FCM] 오래된토큰정리 기준={} 삭제={}", threshold, deleted);
        }
    }
}
