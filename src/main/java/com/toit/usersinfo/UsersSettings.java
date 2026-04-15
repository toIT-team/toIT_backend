package com.toit.usersinfo;

import com.toit.user.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter // 1. 추가
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 2. 추가 (JPA 스펙)
public class UsersSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long usersInfoId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private Users users;

    /** 앱 전체 알림 수신 여부 (알림 설정에서 알림 여부 ) */
    @Column(nullable = false)
    private Boolean appAlarmEnabled = true;

    /***
     * 생성자
     *
     */
    public UsersSettings(Users users) {
        this.users = users;
        this.appAlarmEnabled = true;
    }

    /***
     *     알림 설정 수정 메서드
     */
    public void updateAlarmSettings(Boolean appAlarmEnabled) {
        if (appAlarmEnabled != null) {
            this.appAlarmEnabled = appAlarmEnabled;
        }
    }

}