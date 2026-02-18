package com.toit.swagger.docs.attachments;

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
        @ApiResponse(responseCode = "200", description = "파일 업로드 성공"),
        @ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "UnsupportedFileTypeException",
                                        summary = "잘못된 파일 형식",
                                        value = """
                                        {
                                          "code": 400,
                                          "message": "허용되지 않는 파일 형식입니다."
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
                                )
                        }
                )
        ),
        @ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                name = "AttachmentReadException",
                                value = """
                                {
                                  "code": 500,
                                  "message": "서버에서 파일을 읽지 못 했거나 size를 읽지 못 했습니다."
                                }
                                """
                        )
                )
        )
})
public @interface AttachMentsFilesUploadApiDocs {
}