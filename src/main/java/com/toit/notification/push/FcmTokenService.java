package com.toit.notification.push;


import com.toit.notification.push.request.FcmCreateRequest;
import com.toit.user.Users;
import com.toit.user.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FcmTokenService {
    private final FcmTokenRepository fcmTokenRepository;
    private final UsersService usersService;

    /**
     * 앱이 뜰 때마다 부른다. 그래서 만드는 것과 갱신하는 것을 한 곳에서 처리한다.
     *
     * 기준은 FID 다. 같은 기기가 새 토큰을 들고 와도 줄이 늘지 않고 그 줄이 갱신된다.
     * 기기를 다른 사람이 쓰기 시작했다면 주인도 함께 바뀐다.
     */
    @Transactional
    public void createFcmToken(Long usersId, FcmCreateRequest request) {
        Users users = usersService.findById(usersId);

        // 이 토큰을 다른 기기가 들고 있으면 그 줄을 먼저 치운다.
        // 토큰에 유니크가 걸려 있어 그냥 두면 저장에서 걸린다.
        fcmTokenRepository.findByFcmToken(request.getFcmToken())
                .filter(other -> !other.getInstallationId().equals(request.getInstallationId()))
                .ifPresent(fcmTokenRepository::delete);

        Optional<FcmToken> existing = fcmTokenRepository.findById(request.getInstallationId());

        if (existing.isPresent()) {
            existing.get().refresh(users, request.getFcmToken(),
                    request.getPlatform(), request.getOsVersion());
            return;
        }

        fcmTokenRepository.save(new FcmToken(
                request.getInstallationId(),
                users,
                request.getFcmToken(),
                request.getPlatform(),
                request.getOsVersion()
        ));
    }

    /**
     * 로그아웃할 때 그 기기의 줄만 지운다.
     *
     * 폰에서 로그아웃했다고 태블릿 알림까지 끊기면 안 되므로 FID 로 한 줄만 고른다.
     * 남의 기기를 지우지 못하도록 주인도 함께 확인한다.
     */
    @Transactional
    public void deleteFcmToken(Long usersId, String installationId) {
        fcmTokenRepository.findById(installationId)
                .filter(token -> token.getUsers().getUsersId().equals(usersId))
                .ifPresent(fcmTokenRepository::delete);
    }
}
