package com.toit.user;

import com.toit.common.enums.AuthProvider;
import com.toit.exception.users.UsersNotFoundException;
import com.toit.user.dto.request.UsersCreateRequest;
import com.toit.user.dto.response.UsersCreateResponse;
import java.time.LocalDateTime;

import com.toit.usersinfo.UsersSettings;
import com.toit.usersinfo.UsersSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsersService {
    private final UsersRepository usersRepository;
    private final UsersSettingsRepository usersInfoRepository;


    public UsersCreateResponse createUser(UsersCreateRequest request) {
        Users users = new Users(
                request.getEmail(),
                request.getName(),
                request.getBio(),
                request.getImageUrl(),
                AuthProvider.valueOf(request.getAuthProvider()),
                request.getProviderUsersId(),
                LocalDateTime.now()
        );

        UsersSettings usersInfo = new UsersSettings(users);
        Users user = usersRepository.save(users);
        usersInfoRepository.save(usersInfo);

        return new UsersCreateResponse(user);
    }

    public Users findById(Long usersId){
        return usersRepository.findById(usersId)
                .orElseThrow(() -> new UsersNotFoundException(usersId + "은 존재하지 않는 사용자입니다."));
    }
}
