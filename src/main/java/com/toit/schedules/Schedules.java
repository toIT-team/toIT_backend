package com.toit.schedules;

import com.toit.common.enums.EntityStatus;
import com.toit.folders.Folders;
import com.toit.user.Users;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedules {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long schedulesId;
    /**
     * 스케줄 제목
     * 길이 제한 255
     */
    @Column(nullable = false)
    private String title;

    /***
     * 선택한 컬러 ENUM 값
     */
    private String appColor;

    /***
     * 스케줄이 속한 폴더
     */
    @ManyToOne
    @JoinColumn(name = "folders_id", nullable = true) // 특정 폴더에 속하지 않을 수도 있다면 nullable = true
    private Folders folders;

    /***
     * 스케줄의 시간 설정 여부 (False = 시간 설정 안함 , True = 시간 설정했을 때)
     */
    @Column(nullable = false)
    private Boolean timeSetting;

    /**
     * 스케줄 일정의 시작 날짜
     */
    @Column(nullable = false)
    private LocalDate startDate;

    /**
     * 스케줄 일정의 종료 날짜
     */
    @Column(nullable = false)
    private LocalDate endDate;

    /**
     * 스케줄 일정의 시작 시간
     */
    @Column(nullable = true)
    private LocalTime startTime;

    /**
     * 스케줄 일정의 종료 시간
     */
    @Column(nullable = true)
    private LocalTime endTime;

    /**
     * 스케줄 제목
     * 길이 제한 255
     */
    @Column(nullable = false)
    private String location;

    /**
     * 보관함 서비스 상태
     * ENUM 형태(ACTIVE, DELETED)
     * ACTIVITY일 경우 활성화, DELETED 경우 비홣성화
     */
    @Enumerated(EnumType.STRING)
    private EntityStatus status;

    /**
     * 알림 설정
     */
    @Column(nullable = true)
    private Boolean notification;

    /**
     * 스케줄 상세 메모
     * 길이 제한 1000 (한글 기준 약 333~500자, 바이트 기준 고려)
     */
    @Column(nullable = true) // 255자 이상일 경우 TEXT 타입 권장
    private String memo;

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

    /***
     * 일부 필드만 받는 생성자
     */
    public Schedules(String title, String appColor, Folders folders,
                      Boolean timeSetting, LocalDate startDate, LocalDate endDate,
                      LocalTime startTime, LocalTime endTime,
                      String location, Boolean notification, String memo, Users users) {
        this.title = title;
        this.appColor = appColor;
        this.folders = folders;
        this.timeSetting = timeSetting;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.notification = notification;
        this.memo = memo;
        this.users = users;
        this.status = EntityStatus.ACTIVE; // 초기값 강제
        this.createdAt = LocalDateTime.now(); // 이 줄을 추가하세요!
    }

    /**
     * 스케줄 정보 수정 메서드
     * Service 계층에서 Transactional 안에서 호출되면 Dirty Checking으로 자동 저장됨
     */
    public void update(String title, String appColor, Folders folders,
                       Boolean timeSetting, LocalDate startDate, LocalDate endDate,
                       LocalTime startTime, LocalTime endTime,
                       String location, Boolean notification, String memo) {
        this.title = title;
        this.appColor = appColor;
        this.folders = folders; // 폴더가 변경되거나 null(폴더 없음)로 설정될 수 있음
        this.timeSetting = timeSetting;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.notification = notification;
        this.memo = memo;
    }
}
