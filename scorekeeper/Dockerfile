FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY league-table-events ./league-table-events
COPY league-table-domain ./league-table-domain
COPY scorekeeper ./scorekeeper
COPY score-event-processor/build.gradle ./score-event-processor/build.gradle
COPY table-retriever-service/build.gradle ./table-retriever-service/build.gradle
COPY league-table-domain/build.gradle ./league-table-domain/build.gradle

RUN chmod +x gradlew \
    && ./gradlew :scorekeeper:bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN apk add --no-cache curl

COPY --from=build /workspace/scorekeeper/build/libs/scorekeeper-*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
