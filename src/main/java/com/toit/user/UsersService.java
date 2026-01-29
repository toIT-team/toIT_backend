package com.toit.user;

import com.toit.common.enums.AuthProvider;
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
        Optional<Users> users = usersRepository.findById(usersId);

        if (users.isPresent()) {
            return users.get();
        } else {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다. usersId=" + usersId);
        }
    }
}
