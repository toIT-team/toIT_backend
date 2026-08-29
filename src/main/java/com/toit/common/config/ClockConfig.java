package com.toit.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * 시계를 빈으로 둔다.
 *
 * LocalDateTime.now() 를 직접 부르면 테스트가 시간을 손댈 수 없다. 재시도 간격이
 * 1·2·4분이라 그대로 두면 7분을 실제로 기다려야 검증된다.
 * 테스트에서는 Clock.fixed 로 갈아 끼워 시각을 밀면 된다.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
