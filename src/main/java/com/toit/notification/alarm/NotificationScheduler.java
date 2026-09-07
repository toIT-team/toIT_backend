package com.toit.notification.alarm;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import io.micrometer.core.instrument.MeterRegistry;


@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    /** 최초 발송 뒤 몇 번까지 다시 보낼지. 3번을 쓰면 1+2+4 로 7분을 덮는다. */
    private static final int MAX_ATTEMPT = 3;

    /**
     * 얼마나 지난 알림까지 되살릴지.
     *
     * 사용자가 약속한 것은 일정과의 거리가 아니라 **알림 시각** 자체다. 4시로
     * 맞춘 알림은 오프셋이 5분이든 하루든 4시에 와야 한다. 그래서 유예도 오프셋과
     * 무관하게 하나로 둔다.
     *
     * 10분인 이유는 두 가지다.
     *   - 그쯤까지는 "좀 늦었네" 지만 그 뒤로는 "고장났네" 로 읽힌다고 봤다.
     *     공식 기준은 없어 우리가 정한 값이다.
     *   - 재시도 3회가 1+2+4 로 7분을 쓰므로, 마지막 재시도까지 이 안에 들어간다.
     *     창을 7분 밑으로 내리면 넣어둔 재시도가 잘린다.
     *
     * FCM 쪽에도 같은 개념이 있다. 공식 문서가 짧은 TTL 이 필요한 예로 캘린더
     * 알림을 든다. 다만 몇 분으로 하라는 기준은 없어 그 값은 우리가 정했다.
     * https://firebase.google.com/docs/cloud-messaging/customize-messages/setting-message-lifespan
     */
    private static final long VALID_MINUTES = 10;

    private static final long RUN_TIMEOUT_MINUTES = 9;
    private final SchedulesAlarmRepository schedulesAlarmRepository;
    private final AlarmSendService alarmSendService;
    private final ThreadPoolTaskExecutor alarmExecutor;
    private final MeterRegistry meterRegistry;
    private final Clock clock;

    @Scheduled(cron = "0 * * * * *")
    public void checkAndSendAlerts() {
        LocalDateTime now = LocalDateTime.now(clock).truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime oldest = now.minusMinutes(VALID_MINUTES);

        int expired = schedulesAlarmRepository.markExpired(oldest);
        if (expired > 0) {
            meterRegistry.counter("alarm.expired").increment(expired);
            log.warn("[ALARM] 만료 count={} oldest={}", expired, oldest);
        }

        // "지금 이 1분에 울릴 것" 이 아니라 "아직 안 보낸 것" 을 가져온다.
        // 서버가 꺼졌던 사이에 지나간 알림도 여기서 회수된다.
        List<Long> alarmIds = schedulesAlarmRepository.findTargetAlarmIds(now, oldest);
        log.info("[ALARM] 조회 now={} oldest={} count={}", now, oldest, alarmIds.size());
        if (alarmIds.isEmpty()) return;
        List<Callable<Void>> tasks = alarmIds.stream()
                .map(id -> (Callable<Void>) () -> {
                    alarmSendService.sendOne(id, now);
                    return null;
                })
                .toList();

        // 던져놓고 다 끝날 때까지 여기서 막힌다.
        // 안 기다리면 다음 실행이 아직 보내는 중인 알림을 또 집는다.
        //
        // 중간에 끊길 수 있는 자리가 셋인데, 어디서 끊겨도 유실은 아니다.
        // 못 보낸 것은 PENDING 으로 남아 다음 실행이 회수한다. 다만 창이 10분이라
        // 그 안에 회수되어야 한다.
        //
        //   무슨 일          예외        누가 처리              남은 알림
        //   ─────────────────────────────────────────────────────────────
        //   한 건 실패        O          sendOne 의 catch       다음 실행이 회수
        //   종료 (30초 내)    X          —                      다 끝내고 내려감
        //   종료 (30초 초과)  O          아래 catch             다음 실행이 회수
        //   9분 타임아웃      X          —                      다음 실행이 회수
        //
        // 타임아웃은 예외를 안 던지고 그냥 반환한다. 아직 시작 안 한 것은 취소하고
        // 실행 중인 것은 끊는다.
        try {
            alarmExecutor.getThreadPoolExecutor()
                    .invokeAll(tasks, RUN_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
            log.warn("[ALARM] 실행중단 count={}", alarmIds.size());
        }
        log.info("[ALARM] 실행완료 count={}", alarmIds.size());
    }
}
