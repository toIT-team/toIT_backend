package com.toit.usersinfo;

import com.toit.user.Users;
import com.toit.user.UsersService;
import com.toit.exception.userssettings.UsersSettingsNotFoundException;
import com.toit.user.dto.request.UsersUpdateNameRequest;
import com.toit.usersinfo.dto.request.UsersSettingsUpdateRequest;
import com.toit.usersinfo.dto.response.UsersSettingsUpdateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsersSettingsService {
    private final UsersService usersService;
    private final UsersSettingsRepository usersSettingsRepository;


    public UsersSettingsUpdateResponse updateAlarm(Long usersId, UsersSettingsUpdateRequest request) {
        Users user = usersService.findById(usersId);
        UsersSettings usersSettings = usersSettingsRepository.findByUsers_UsersId(user.getUsersId());
        if (usersSettings == null) {
            throw new UsersSettingsNotFoundException("사용자 설정 정보를 찾을 수 없습니다.");
        }
        usersSettingsRepository.save(usersSettings);

        return new UsersSettingsUpdateResponse(usersSettings.getAppAlarmEnabled());


    }

}
