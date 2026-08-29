package com.toit.notification.push;

/**
 * 발송 결과. 스케줄러가 상태를 정하려면 성공 여부만으로는 부족하다.
 *
 * 전에는 boolean 하나를 돌려줘서 "앱 알림 꺼짐", "토큰 없음", "FCM 이 거부함" 이
 * 전부 false 로 뭉쳤다. 그러면 무엇으로 기록할지 정할 수가 없다.
 */
public record FcmSendResult(Outcome outcome, String errorCode) {

    public enum Outcome {
        /** 하나라도 도착했다. 기기가 여러 대면 하나만 성공해도 사용자는 알았다. */
        SENT,

        /** 보낼 토큰이 없다. 몇 분 뒤에도 없으므로 재시도할 이유가 없다. */
        NO_TOKEN,

        /** 사용자가 앱 알림을 껐다. 실패가 아니다. */
        ALARM_OFF,

        /** 보낼 데는 있는데 실패했다. 다시 보내볼 수 있다. */
        FAILED
    }

    /** FCM 이 오류 코드조차 주지 않았을 때 (타임아웃 · 네트워크 단절) 남기는 값 */
    public static final String TIMEOUT = "TIMEOUT";

    public static FcmSendResult sent() {
        return new FcmSendResult(Outcome.SENT, null);
    }

    public static FcmSendResult noToken() {
        return new FcmSendResult(Outcome.NO_TOKEN, "NO_TOKEN");
    }

    public static FcmSendResult alarmOff() {
        return new FcmSendResult(Outcome.ALARM_OFF, "ALARM_OFF");
    }

    public static FcmSendResult failed(String errorCode) {
        return new FcmSendResult(Outcome.FAILED, errorCode == null ? TIMEOUT : errorCode);
    }

    public boolean isSent() {
        return outcome == Outcome.SENT;
    }
}
