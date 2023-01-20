# syntax=docker/dockerfile:1

FROM openjdk:17
ARG RELEASE_VERSION

WORKDIR /app

ADD target/arduino-server-${RELEASE_VERSION}.jar ./arduino-server.jar

ENTRYPOINT ["java", "-jar", "arduino-server.jar"]
EXPOSE 6651