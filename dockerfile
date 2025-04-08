# 1. 빌더 스테이지 이미지 변경 (표준 eclipse-temurin JDK 사용)
FROM eclipse-temurin:17-jdk AS builder_stage

WORKDIR /app

COPY ./gradlew .
COPY ./gradle ./gradle

RUN chmod +x ./gradlew

COPY ./src ./src
COPY ./build.gradle .
COPY ./settings.gradle .

# Gradle 빌드 실행 (테스트 제외, 데몬 사용 안함)
RUN ./gradlew bootJar -x test --no-daemon

# ---

# 2. 런타임 스테이지 이미지 변경 (표준 eclipse-temurin JRE 사용)
FROM eclipse-temurin:17-jre AS runtime_stage

WORKDIR /app

# 빌더 스테이지에서 빌드된 JAR 파일 복사
COPY --from=builder_stage /app/build/libs/walkinggo-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]