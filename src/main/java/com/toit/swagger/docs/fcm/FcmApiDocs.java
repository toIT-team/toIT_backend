package com.toit.swagger.docs.fcm;

import com.toit.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "FCM토큰 업로드 성공"),
        @ApiResponse(
                responseCode = "404",
                description = "존재하지 않음",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "UsersNotFound",
                                        summary = "사용자가 존재하지 않음",
                                        value = """
                                        {
                                          "code": 404,
                                          "message": "1은 존재하지 않는 사용자입니다."
                                        }
                                        """
                                ),
                        }
                )
        )
})

public @interface FcmApiDocs {
}
