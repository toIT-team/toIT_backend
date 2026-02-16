package com.toit.fcm;


import com.toit.fcm.request.FcmCreateRequest;
import com.toit.fcm.response.FcmCreateResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
            summary = "FcmToken 생성 ",
            description = "Token 생성은 POST입니다."
    )
    @PostMapping()
    public ResponseEntity<FcmCreateResponse> createFcmToken(
            @RequestBody @Valid FcmCreateRequest request
    ){
        return ResponseEntity.ok(fcmTokenService.createFcmToken(request));

    }
}
