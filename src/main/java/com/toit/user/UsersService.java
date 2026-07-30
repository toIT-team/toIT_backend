package com.toit.user;

import com.toit.common.enums.AuthProvider;
import com.toit.exception.users.InvalidUsersNameException;
import com.toit.exception.users.UsersNotFoundException;
import com.toit.user.dto.request.UsersCreateRequest;
import com.toit.user.dto.request.UsersUpdateNameRequest;
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
    private final UsersWithdrawService usersWithdrawService;

    /** 닉네임 최대 길이 */
    private static final int NAME_MAX_LENGTH = 10;


    public UsersCreateResponse createUser(UsersCreateRequest request) {
        Users users = new Users(
                request.getEmail(),
                request.getName(),
                request.getBio(),
                request.getImageUrl(),
                AuthProvider.valueOf(request.getAuthProvider()),
                String.valueOf(request.getProviderUsersId()),
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

    public void updateName(Long usersId, UsersUpdateNameRequest request) {
        String name = validateName(request.getName());

        Users user = findById(usersId);
        user.updateName(name);
        usersRepository.save(user);
    }

    /**
     * 닉네임 검증 - 앞뒤 공백을 제거한 뒤 검사하고, 정제된 값을 반환한다.
     * 공백만 입력한 경우도 빈 값으로 처리한다.
     */
    private String validateName(String name) {
        String trimmed = (name == null) ? "" : name.strip();

        if (trimmed.isEmpty()) {
            throw new InvalidUsersNameException("닉네임을 입력해주세요.");
        }
        if (trimmed.length() > NAME_MAX_LENGTH) {
            throw new InvalidUsersNameException("닉네임은 최대 " + NAME_MAX_LENGTH + "자까지 입력할 수 있습니다.");
        }
        return trimmed;
    }

    /**
     * 회원 탈퇴 - 사용자와 관련된 모든 데이터를 완전 삭제한다. (복구 불가)
     */
    public void withdraw(Long usersId) {
        usersWithdrawService.withdraw(findById(usersId));
    }
}
