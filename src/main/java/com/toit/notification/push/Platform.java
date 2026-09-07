package com.toit.notification.push;

/**
 * 토큰이 붙어 있는 기기의 종류.
 *
 * 발송 옵션이 플랫폼마다 갈린다. 유효 기간만 해도 Android 는 AndroidConfig.setTtl,
 * iOS 는 apns-expiration 헤더로 넣어야 한다. 지금은 어느 쪽인지 몰라 둘 다 안 걸고
 * 있는데, 이 값이 생기면 갈라서 넣을 수 있다.
 */
public enum Platform {
    ANDROID,
    IOS
}
