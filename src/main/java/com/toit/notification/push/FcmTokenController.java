package com.toit.notification.push;


import com.toit.notification.push.request.FcmCreateRequest;
import com.toit.swagger.docs.fcm.FcmApiDocs;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import com.toit.common.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fcm")
@RequiredArgsConstructor
public class FcmTokenController {

    private final FcmTokenService fcmTokenService ;

    @Operation(
            summary = "FcmToken 등록",
            description = "앱이 뜰 때마다 부릅니다. 같은 기기(installationId)면 새로 만들지 않고 갱신합니다."
    )
    @PostMapping()
    @FcmApiDocs
    public ResponseEntity<Void> createFcmToken(
            @RequestBody @Valid FcmCreateRequest request
    ){
        Long usersId = SecurityUtil.getCurrentUserId();
        fcmTokenService.createFcmToken(usersId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "FcmToken 삭제",
            description = "로그아웃할 때 그 기기의 토큰만 지웁니다. 다른 기기는 그대로 둡니다."
    )
    @DeleteMapping("/{installationId}")
    public ResponseEntity<Void> deleteFcmToken(
            @PathVariable("installationId") String installationId
    ) {
        Long usersId = SecurityUtil.getCurrentUserId();
        fcmTokenService.deleteFcmToken(usersId, installationId);
        return ResponseEntity.noContent().build();
    }
}
