package com.toit.entity;

import com.toit.enums.EntityStatus;
import com.toit.enums.ItemsType;
import com.toit.enums.StorageTarget;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Table(name = "items")
public class Items {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemsId;

    /**
     * 자료가 저장된 곳
     * ENUM형태("FOLDERS", "SCHEDULE")
     */
    @Enumerated(EnumType.STRING)
    private StorageTarget storageTarget;

    /**
     * 폴더 및 일정의 식별자 값
     */
    @Column(nullable = false)
    private Long storageId;

    /**
     * 자료형의 형태
     * ENUM형태("LINK", "IMAGE", "FILE", "TEXT")
     */
    @Enumerated(EnumType.STRING)
    private ItemsType itemsType;

    /**
     * 자료 이름
     * 길이 제한 255
     */
    @Column(nullable = false)
    private String name;

    /**
     * 링크 URL
     * type형태가 LINK일 때 사용
     */
    @Column(nullable = true, length = 2000)
    private String url;

    /**
     * 파일 경로
     * type형태가 FILE, IMAGE일 때 사용
     */
    @Column(nullable = true, length = 2000)
    private String filePath;

    /**
     * 파일 경로
     * type형태가 TEXT, IMAGE일 때 사용
     * IMAGE의 경우 설명이 이 값에 포함
     */
    @Column(nullable = true, length = 2000)
    private String textContent;

    /**
     * 자료 서비스 상태
     * ENUM 형태(ACTIVE, DELETED)
     * ACTIVITY일 경우 활성화, DELETED 경우 비홣성화
     */
    @Enumerated(EnumType.STRING)
    private EntityStatus status;

    /**
     * 자료 생성 후 시간 즉시 생성
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 자료 서비스 상태가 DELETED 되면 업데이트
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
