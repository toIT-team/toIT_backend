package com.toit.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 알림 발송을 나눠 맡을 스레드 풀
 *
 * 한 건의 85%가 FCM 답을 기다리는 시간이라, 여럿이 같이 기다리면 그만큼 겹친다.
 * CPU를 더 쓰는 게 아니라 대기 시간이 포개지는 것
 */
@Configuration
public class AlarmExecutorConfig {
    @Bean
    public ThreadPoolTaskExecutor alarmExecutor(
            @Value("${alarm.send.concurrency}") int concurrency
    ){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //코어와 최대를 같게 둔다. 큐에 쌓이다. 뒤늦게 스레드가 느는 것보다,
        // 정해진 수가 계쏙 떠 있는 편이 상한을 읽기 쉽다
        executor.setCorePoolSize(concurrency); // 기본으로 유지하는 스레드 수
        executor.setMaxPoolSize(concurrency); // 줄이 꽉 찼을 때 늘릴 수 있는 최대
        //로그에서 어느 스레드가 무엇을 했는지 갈라 보려고 붙인다.
        executor.setThreadNamePrefix("alarm-send-"); // 스레드 이름 정하기
        //배포로 내려갈 때 보내는 중인 것을 기다려준다. 발송은 나갔는데 상태를
        //못 적고 죽으면 다음 실행이 또 보낸다.
        executor.setWaitForTasksToCompleteOnShutdown(true); // 서버가 정상 종료 되려고 할 때 스레드가 작업하던 거 30초 동안 하고 안되면 그냥 끄기
        executor.setAwaitTerminationSeconds(30);
        return executor;
    }

}
