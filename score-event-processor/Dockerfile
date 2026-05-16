FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY cqrs-events ./cqrs-events
COPY score-event-processor ./score-event-processor
COPY table-retriever-service/build.gradle ./table-retriever-service/build.gradle

RUN chmod +x gradlew \
    && ./gradlew :score-event-processor:bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN apk add --no-cache wget

COPY --from=build /workspace/score-event-processor/build/libs/score-event-processor-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
