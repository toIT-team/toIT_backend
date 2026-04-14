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


    public FcmCreateResponse createFcmToken(Long usersId, FcmCreateRequest request) {
        Users users = usersService.findById(usersId);

        Optional<FcmToken> existingToken = fcmTokenRepository.findByUsersAndFcmToken(users, request.getFcmToken());

        if (existingToken.isPresent()) {
            existingToken.get().updateTokenTimestamp();
            return new FcmCreateResponse(usersId, request.getFcmToken());
        } else {
            FcmToken newToken = new FcmToken(users, request.getFcmToken());
            fcmTokenRepository.save(newToken);
            return new FcmCreateResponse(usersId, request.getFcmToken());
        }
    }
}
