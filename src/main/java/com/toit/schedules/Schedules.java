package com.toit.schedules;

import com.toit.common.enums.EntityStatus;
import com.toit.user.Users;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;

@Entity
public class Schedules {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long schedulesId;

    /**
     * 스케줄 제목
     * 길이 제한 255
     */
    @Column(nullable = false)
    private String title;

    /**
     * 스케줄 일정의 시작 날짜 및 시간
     */
    @Column(nullable = true)
    private LocalDateTime startAt;

    /**
     * 스케줄 일정의 종료 날짜 및 시간
     */
    @Column(nullable = true)
    private LocalDateTime endAt;

    /**
     * 보관함 서비스 상태
     * ENUM 형태(ACTIVE, DELETED)
     * ACTIVITY일 경우 활성화, DELETED 경우 비홣성화
     */
    @Enumerated(EnumType.STRING)
    private EntityStatus status;

    /**
     * 스케줄 생성 후 시간 즉시 생성
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 스케줄 서비스 상태가 DELETED 되면 업데이트
     */
    @Column(nullable = true)
    private LocalDateTime deletedAt;

    /**
     * Users와 N:1 관계 설정
     */
    @ManyToOne
    @JoinColumn(name = "users_id", nullable = false)
    private Users users;

}
