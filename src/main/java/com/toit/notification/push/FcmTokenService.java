package com.toit.notification.push;


import com.toit.notification.push.request.FcmCreateRequest;
import com.toit.notification.push.response.FcmCreateResponse;
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

        Optional<FcmToken> existingToken = fcmTokenRepository.findByFcmToken(request.getFcmToken());

        if (existingToken.isPresent()) {
            FcmToken fcmToken = existingToken.get();
            if (fcmToken.getUsers().getUsersId().equals(usersId)) {
                fcmToken.updateTokenTimestamp();
            } else {
                fcmToken.updateUser(users);
            }
            fcmTokenRepository.save(fcmToken);
            return new FcmCreateResponse(usersId, request.getFcmToken());
        } else {
            FcmToken newToken = new FcmToken(users, request.getFcmToken());
            fcmTokenRepository.save(newToken);
            return new FcmCreateResponse(usersId, request.getFcmToken());
        }
    }

    public void deleteFcmToken(Long usersId, FcmCreateRequest request) {
        Users users = usersService.findById(usersId);
        fcmTokenRepository.findByUsersAndFcmToken(users, request.getFcmToken())
                .ifPresent(fcmTokenRepository::delete);
    }
}
