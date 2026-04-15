package com.toit.noitce;


import com.toit.admin.Admin;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notice")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noticeId;

    /**
     * 공지사항 제목
     */
    @Column(nullable = false,length = 255)
    private String title;


    /**
     * 공지사항 내용
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 공지 작성 관리자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;


    /**
     * 생성 날짜
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime deletedAt;

    public Notice(String title, String content, Admin admin, LocalDateTime createdAt) {
        this.title = title;
        this.content = content;
        this.admin = admin;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    /**
     * 공지사항 수정 - PUT 방식으로 전체 값을 갱신한다.
     */
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }


}
