package com.toit.entity;

import com.toit.enums.EntityStatus;
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

    @Column(nullable = false)
    private String title;

    @Column(nullable = true)
    private LocalDateTime startAt;

    @Column(nullable = true)
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    private EntityStatus status;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private LocalDateTime deletedAt;

    /**
     * 사용자 엔티티
     * OAuth 로그인 및 소프트 삭제를 지원한다.
     * Users와 N:1 관계 설정
     */
    @ManyToOne
    @JoinColumn(name = "users_id", nullable = false)
    private Users users;

}
