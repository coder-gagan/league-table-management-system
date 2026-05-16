FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY cqrs-events ./cqrs-events
COPY table-retriever-service ./table-retriever-service
COPY score-event-processor/build.gradle ./score-event-processor/build.gradle

RUN chmod +x gradlew \
    && ./gradlew :table-retriever-service:bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN apk add --no-cache curl

COPY --from=build /workspace/table-retriever-service/build/libs/table-retriever-service-*.jar app.jar

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
