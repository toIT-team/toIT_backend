package com.toit.swagger.docs.items.texts;


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
        @ApiResponse(responseCode = "200", description = "링크 업로드 성공"),
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
        )
})
public @interface TextsCreateApiDocs {
}
