FROM eclipse-temurin:17-jdk AS builder_stage

WORKDIR /app

COPY ./gradlew .
COPY ./gradle ./gradle

RUN chmod +x ./gradlew

COPY ./src ./src
COPY ./build.gradle .
COPY ./settings.gradle .

RUN ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre AS runtime_stage

WORKDIR /app

COPY --from=builder_stage /app/build/libs/walkinggo-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# 처음 빌드 할때 docker-compose up --build & 수정했을때도
# 그냥 껏다 키는 경우는 docker-compose down
# 위로 껏으면 docker-compose up 이걸로 키면됨