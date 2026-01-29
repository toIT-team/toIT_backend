package com.toit.user;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsersService {
    private final UsersRepository usersRepository;

    public Users findById(Long usersId){
        Optional<Users> users = usersRepository.findById(usersId);

        if (users.isPresent()) {
            return users.get();
        } else {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다. usersId=" + usersId);
        }
    }
}
