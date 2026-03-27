package com.toit;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;


@EnableScheduling //  스케줄러에 필요한 애노테이션
@EnableJpaAuditing // @CreatedDate 에 필요한 애노테이션
@SpringBootApplication
public class ToitApplication {

	@PostConstruct
	public void started() {
		// 서버 시간을  한국 시간으로 고정
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
	}

	public static void main(String[] args) {
		SpringApplication.run(ToitApplication.class, args);
	}

}
