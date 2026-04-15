package com.toit.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "toIT API 문서",
                description = "Swagger를 이용한 toIT 백엔드 API 문서입니다.",
                version = "0.0.0"
        ),
        servers = {
                @Server(url = "https://api.toit.cloud", description = "운영 서버"),
                @Server(url = "http://localhost:8080", description = "로컬 서버")
        }
)
@Configuration
public class SwaggerConfig {

}