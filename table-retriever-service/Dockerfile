FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY league-table-events ./league-table-events
COPY league-table-domain ./league-table-domain
COPY table-retriever-service ./table-retriever-service
COPY league-table-domain/build.gradle ./league-table-domain/build.gradle
COPY scorekeeper/build.gradle ./scorekeeper/build.gradle
COPY score-event-processor/build.gradle ./score-event-processor/build.gradle

RUN chmod +x gradlew \
    && ./gradlew :table-retriever-service:bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN apk add --no-cache curl

COPY --from=build /workspace/table-retriever-service/build/libs/table-retriever-service-*.jar app.jar

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
