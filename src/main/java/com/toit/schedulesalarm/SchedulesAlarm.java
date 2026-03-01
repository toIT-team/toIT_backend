package com.toit.schedulesalarm;

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
     * 발송 상태 (보내졌는지 안 보내졌는지)
     */
    @Column(nullable = false)
    private Boolean isSent;

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
        this.isSent = false;
        this.isRead = false;
    }


    public void updateAlarm(Boolean alarmState, LocalDateTime alarmDateTime, Long alarmOffsetMinutes) {
        this.alarmState = alarmState;
        this.alarmDateTime = alarmDateTime;
        this.alarmOffsetMinutes = alarmOffsetMinutes;
        // 알림이 수정되면 발송 및 읽음 상태 초기화
        this.isSent = false;
        this.isRead = false;
    }


    public void markAsSent() {
        this.isSent = true;
    }

    public void markAsRead() {
        this.isRead = true;
    }

}
