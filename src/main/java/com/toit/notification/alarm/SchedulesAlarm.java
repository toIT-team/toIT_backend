package com.toit.notification.alarm;

import com.toit.schedules.Schedules;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SchedulesAlarm {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long schedulesAlarmId;

    // --- [1:1 연관관계 추가] ---
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedules_id", nullable = false)
    private Schedules schedules;

    /***
     * 알림 설정 여부 (켰는지 , 안 켰는지)
     */
    @Column(nullable = false)
    private Boolean alarmState;

    /***
     * 계산된 알림 시간
     */
    private LocalDateTime alarmDateTime;

    /***
     * 정수타입의 몇분전에 알림을 설정 했는지 (5분,10분 , 직접 설정)
     */
    private Long alarmOffsetMinutes;


    /***
     * 발송 상태. PENDING 인 것만 스케줄러가 집어간다.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlarmStatus status;

    /***
     * 마지막으로 실패했을 때의 오류 코드.
     * 로직에는 쓰지 않고 나중에 왜 실패했는지 세려고 남긴다.
     */
    @Column(length = 50)
    private String lastErrorCode;

    /***
     * 재시도한 횟수. 최초 발송은 포함하지 않는다.
     */
    @Column(nullable = false)
    private Integer attemptCount;

    /***
     * 다음에 다시 시도할 시각. NULL 이면 아직 한 번도 실패하지 않은 것이다.
     */
    private LocalDateTime nextAttemptAt;

    /***
     * 발송 상태 (보내졌는지 안 보내졌는지)
     */
    @Column(nullable = false)
    private Boolean isRead;

    // --- 생성자 추가---
    public SchedulesAlarm(Schedules schedules, Boolean alarmState, LocalDateTime alarmDateTime, Long alarmOffsetMinutes) {
        this.schedules = schedules;
        this.alarmState = alarmState;
        this.alarmDateTime = alarmDateTime;
        this.alarmOffsetMinutes = alarmOffsetMinutes;
        this.status = AlarmStatus.PENDING;
        this.attemptCount = 0;
        this.isRead = false;
    }


    public void updateAlarm(Boolean alarmState, LocalDateTime alarmDateTime, Long alarmOffsetMinutes) {
        this.alarmState = alarmState;
        this.alarmDateTime = alarmDateTime;
        this.alarmOffsetMinutes = alarmOffsetMinutes;
        // 알림이 수정되면 발송 이력을 처음으로 되돌린다.
        // 재시도 예산까지 되돌리지 않으면 미룬 알림이 한 번 보고 바로 접힌다.
        this.status = AlarmStatus.PENDING;
        this.attemptCount = 0;
        this.nextAttemptAt = null;
        this.lastErrorCode = null;
        this.isRead = false;
    }


    /** 하나라도 도착했다. 조회에서 빠진다. */
    public void markAsSent() {
        this.status = AlarmStatus.SENT;
        this.nextAttemptAt = null;
    }

    /** 더 보내지 않는다. 성공은 아니다. */
    public void markAsFailed(String errorCode) {
        this.status = AlarmStatus.FAILED;
        this.nextAttemptAt = null;
        this.lastErrorCode = errorCode;
    }

    /** PENDING 으로 두고 다음 시도 시각을 적는다. */
    public void scheduleNextAttempt(LocalDateTime nextAttemptAt, String errorCode) {
        this.attemptCount = this.attemptCount + 1;
        this.nextAttemptAt = nextAttemptAt;
        this.lastErrorCode = errorCode;
    }

    public void markAsRead() {
        this.isRead = true;
    }

}
