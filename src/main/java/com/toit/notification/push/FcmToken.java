package com.toit.notification.push;

import com.toit.user.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 기기 하나에 한 줄.
 *
 * 예전에는 토큰 값이 유일한 식별자였다. 그런데 토큰은 회전한다. 같은 기기가 새 토큰을
 * 들고 오면 옛 줄을 못 찾아 새 줄이 생기고, 옛 줄은 주인 없이 남았다.
 *
 * FID(Firebase installation ID)는 앱을 지웠다 깔지 않는 한 그대로다. 그래서 이것을
 * 식별자로 쓰면 토큰이 바뀌어도 같은 줄을 갱신하게 된다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmToken {

    /** FID. 실제로는 22자지만 형식이 바뀔 여지를 두고 넉넉히 잡는다. */
    @Id
    @Column(length = 64)
    private String installationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private Users users;

    /** 같은 토큰이 두 기기에 붙는 일은 없어야 한다. */
    @Column(nullable = false, unique = true)
    private String fcmToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Platform platform;

    /** "14" · "17.2" 처럼 숫자만. 플랫폼은 위 컬럼에 따로 있다. */
    @Column(length = 20)
    private String osVersion;

    @Column(nullable = false)
    private LocalDateTime lastUpdatedAt;

    public FcmToken(String installationId, Users users, String fcmToken,
                    Platform platform, String osVersion) {
        this.installationId = installationId;
        this.users = users;
        this.fcmToken = fcmToken;
        this.platform = platform;
        this.osVersion = osVersion;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    /**
     * 같은 기기가 다시 등록했다.
     *
     * 토큰이 그대로여도 시각은 갱신한다. 오래 안 쓴 기기를 걸러내는 배치가 이 값을
     * 보기 때문이다.
     */
    public void refresh(Users users, String fcmToken, Platform platform, String osVersion) {
        this.users = users;
        this.fcmToken = fcmToken;
        this.platform = platform;
        this.osVersion = osVersion;
        this.lastUpdatedAt = LocalDateTime.now();
    }
}
