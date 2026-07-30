package com.toit.user;

import com.toit.common.SecurityUtil;
import com.toit.user.dto.request.UsersCreateRequest;
import com.toit.user.dto.request.UsersUpdateNameRequest;
import com.toit.user.dto.response.UsersCreateResponse;
import com.toit.usersinfo.UsersSettingsService;
import com.toit.usersinfo.dto.request.UsersSettingsUpdateRequest;
import com.toit.usersinfo.dto.response.UsersSettingsUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {

    private final UsersService usersService;
    private final UsersSettingsService usersSettingsService;

    /** 사용자 생성 - 카카오 로그인 전 임시 */
    @Operation(
            summary = "사용자 생성 API - 화면이름 : X(임시 API)",
            description = "로그인이 없어 임시로 사용자를 생성해서 해야됩니다."
    )
    @PostMapping
    public ResponseEntity<UsersCreateResponse> createUser(
            @RequestBody UsersCreateRequest request
    ) {
        return ResponseEntity.ok(usersService.createUser(request));
    }

    @Operation(
            summary = "이름 수정 API",
            description = "사용자의 이름(닉네임)을 수정합니다. 닉네임은 최대 10자까지 입력할 수 있습니다."
    )
    @PatchMapping("/name")
    public ResponseEntity<Void> updateName(@RequestBody UsersUpdateNameRequest request) {
        Long usersId = SecurityUtil.getCurrentUserId();
        usersService.updateName(usersId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "회원 탈퇴 API",
            description = "회원 탈퇴 시 계정과 관련된 모든 데이터(보관함, 자료, 일정, 업로드 파일)가 완전히 삭제됩니다. "
                    + "복구할 수 없으며, 문의 내역은 작성자를 익명화한 채 운영 자료로 보관됩니다."
    )
    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdraw() {
        Long usersId = SecurityUtil.getCurrentUserId();
        usersService.withdraw(usersId);
        return ResponseEntity.noContent().build();
    }

    //유저 알림설정 변경 (on,off)
    @PatchMapping("/alarmSetting")
    public UsersSettingsUpdateResponse updateName(@RequestBody UsersSettingsUpdateRequest request) {
        Long usersId = SecurityUtil.getCurrentUserId();
        usersSettingsService.updateAlarm(usersId,request);

        return usersSettingsService.updateAlarm(usersId,request);
    }




}
