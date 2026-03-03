package com.toit.swagger.docs.items.attachments;

import com.toit.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "파일 이름 수정 성공"),
        @ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "AttachmentFileNameUpdateNotAllowedException",
                                        summary = "FILE 타입이 아님",
                                        value = """
                                        {
                                          "code": 400,
                                          "message": "attachmentsType이 FILE인 경우에만 fileName을 수정할 수 있습니다."
                                        }
                                        """
                                )
                        }
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "존재하지 않음",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "UsersNotFound",
                                        summary = "사용자 존재하지 않음",
                                        value = """
                                        {
                                          "code": 404,
                                          "message": "1은 존재하지 않는 사용자입니다."
                                        }
                                        """
                                ),
                                @ExampleObject(
                                        name = "FoldersNotFound",
                                        summary = "보관함 존재하지 않음",
                                        value = """
                                        {
                                          "code": 404,
                                          "message": "1은 존재하지 않는 보관함입니다."
                                        }
                                        """
                                ),
                                @ExampleObject(
                                        name = "AttachmentsNotFoundException",
                                        summary = "이미지/파일 존재하지 않음",
                                        value = """
                                        {
                                          "code": 404,
                                          "message": "1는 존재하지 않는 이미지/파일입니다."
                                        }
                                        """
                                )
                        }
                )
        )
})
public @interface AttachMentsUpdateFileNameApiDocs {
}
