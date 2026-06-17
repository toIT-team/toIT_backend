package com.toit.fcm;


import com.toit.fcm.request.FcmCreateRequest;
import com.toit.fcm.response.FcmCreateResponse;
import com.toit.swagger.docs.fcm.FcmApiDocs;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import com.toit.common.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fcm")
@RequiredArgsConstructor
public class FcmTokenController {

    private final FcmTokenService fcmTokenService ;

    // [FCM 비활성화] 토큰 등록/삭제 API 비활성화 (라우팅 제거)
    // @Operation(
    //         summary = "FcmToken 생성 ",
    //         description = "Token 생성은 POST입니다."
    // )
    // @PostMapping()
    // @FcmApiDocs
    // public ResponseEntity<FcmCreateResponse> createFcmToken(
    //         @RequestBody @Valid FcmCreateRequest request
    // ){
    //     Long usersId = SecurityUtil.getCurrentUserId();
    //     return ResponseEntity.ok(fcmTokenService.createFcmToken(usersId, request));
    // }

    // @Operation(
    //         summary = "FcmToken 삭제",
    //         description = "현재 로그인한 사용자의 특정 FCM 토큰을 삭제합니다."
    // )
    // @DeleteMapping()
    // public ResponseEntity<Void> deleteFcmToken(
    //         @RequestBody @Valid FcmCreateRequest request
    // ) {
    //     Long usersId = SecurityUtil.getCurrentUserId();
    //     fcmTokenService.deleteFcmToken(usersId, request);
    //     return ResponseEntity.noContent().build();
    // }
}
