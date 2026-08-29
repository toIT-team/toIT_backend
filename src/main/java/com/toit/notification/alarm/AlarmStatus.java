package com.toit.notification.alarm;

/**
 * 알림 예약의 발송 상태.
 *
 * 불리언 하나로는 "성공" 과 "보내봐야 소용없어 접음" 을 구분할 수 없어 상태로 나눴다.
 * 셋이 동시에 참일 수 없으므로 컬럼을 여러 개 두지 않고 하나에 모았다.
 */
public enum AlarmStatus {

    /** 아직 안 보냄. 스케줄러의 조회 대상이다. */
    PENDING,

    /** 보냈다. 하나라도 도착했으면 성공으로 본다. */
    SENT,

    /** 더 안 보지만 성공은 아니다. 왜 접었는지는 lastErrorCode 로 갈린다. */
    FAILED
}
