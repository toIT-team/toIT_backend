package com.toit.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "toIT API 문서",
                description = "Swagger를 이용한 toIT 백엔드 API 문서입니다.",
                version = "0.0.0"
        )
)
@Configuration
public class SwaggerConfig {

}