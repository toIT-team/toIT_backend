package com.toit.folders;

import com.toit.common.enums.EntityStatus;
import com.toit.user.Users;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;


@Entity
@Table(name = "folders")
public class Folders {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long foldersId;

    /**
     * 보관함 이름
     * 길이 제한 50
     */
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * 보관함 메모
     * 있어도 되고, 없어도 됨
     * 길이 제한 255
     */
    @Column(nullable = true)
    private String memo;

    /**
     * 보관함 기본 보관함 여부
     * 기본 보관함 이면 1, 기본 보관함이 아니면 0
     */
    private Boolean isDefault;

    /**
     * 보관함 색깔 지정 -> #FFAAOO UI용 색상값
     */
    @Column(length = 20)
    private String color;

    /**
     * 보관함 서비스 상태
     * ENUM 형태(ACTIVE, DELETED)
     * ACTIVITY일 경우 활성화, DELETED 경우 비홣성화
     */
    @Enumerated(EnumType.STRING)
    private EntityStatus status;

    /**
     * 보관함 생성 후 시간 즉시 생성
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 보관함 서비스 상태가 DELETED 되면 업데이트
     */
    @Column(nullable = true)
    private LocalDateTime deletedAt;

    /**
     * 보관함 즐겨찾기 기능
     * 즐겨찾기 설정 -> 1, 즐겨찾기 비설정 -> 0
     */
    private Boolean isFavorite;


    @ManyToOne
    @JoinColumn(name = "users_id", nullable = false)
    private Users users;



}
