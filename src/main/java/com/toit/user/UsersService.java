package com.toit.user;

import com.toit.common.enums.AuthProvider;
import com.toit.exception.users.UsersNotFoundException;
import com.toit.user.dto.request.UsersCreateRequest;
import com.toit.user.dto.response.UsersCreateResponse;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsersService {
    private final UsersRepository usersRepository;


    public UsersCreateResponse createUser(UsersCreateRequest request) {
        Users users = new Users(
                request.getEmail(),
                request.getName(),
                request.getBio(),
                AuthProvider.valueOf(request.getAuthProvider()),
                request.getProviderUsersId(),
                LocalDateTime.now()
        );
        return new UsersCreateResponse(usersRepository.save(users));
    }

    public Users findById(Long usersId){
        return usersRepository.findById(usersId)
                .orElseThrow(() -> new UsersNotFoundException("usersId가 " + usersId + "인 해당 사용자를 찾을 수 없습니다."));
    }
}
