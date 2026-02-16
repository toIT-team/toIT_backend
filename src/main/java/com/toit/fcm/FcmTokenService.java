package com.toit.fcm;


import com.toit.fcm.request.FcmCreateRequest;
import com.toit.fcm.response.FcmCreateResponse;
import com.toit.user.Users;
import com.toit.user.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FcmTokenService {
    private final FcmTokenRepository fcmTokenRepository;
    private final UsersService usersService;


    public FcmCreateResponse createFcmToken(FcmCreateRequest request) {
        Users users = usersService.findById(request.getUsersId());

        // 2. 기존에 해당 유저가 이 토큰을 가지고 있는지 확인
        Optional<FcmToken> existingToken = fcmTokenRepository.findByUsersAndFcmToken(users, request.getFcmToken());

        if (existingToken.isPresent()) {
            // [Case 1] 이미 있는 토큰 -> 시간만 갱신 (Update)
            existingToken.get().updateTokenTimestamp();



            return new FcmCreateResponse(request.getUsersId(), request.getFcmToken());
        } else {
            // [Case 2] 처음 보는 토큰 -> 새로 저장 (Insert)
            FcmToken newToken = new FcmToken(users, request.getFcmToken());

            fcmTokenRepository.save(newToken);

            return new FcmCreateResponse(request.getUsersId(), request.getFcmToken());
        }

    }
}
