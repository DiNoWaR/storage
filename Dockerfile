FROM openjdk:17-jdk-slim AS builder

WORKDIR /app
COPY . .

RUN chmod +x gradlew
RUN ./gradlew test
RUN ./gradlew bootJar

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
