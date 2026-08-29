# ===== build stage =====
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Gradle 래퍼 + 빌드 스크립트 먼저 복사 (의존성 캐시 활용)
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY src ./src

RUN chmod +x gradlew && ./gradlew bootJar --no-daemon -x test

# ===== run stage =====
FROM eclipse-temurin:21-jre
WORKDIR /app

# 베이스 이미지 기본값이 UTC 라서 자정 날짜가 하루 전으로 밀린다.
# ToitApplication 의 @PostConstruct 는 로깅·커넥션 생성보다 늦어 여기서 잡아야 한다.
ENV TZ=Asia/Seoul
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
# JAVA_OPTS 는 compose 에서 주입 (힙/메타스페이스 제한)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
