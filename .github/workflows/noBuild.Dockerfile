# Create docker image with only distribution jar

FROM adoptopenjdk/openjdk11:alpine

ARG JAR_FILE
COPY ${JAR_FILE} /tessera/tessera-app.jar

ENTRYPOINT ["java", "-jar", "/tessera/tessera-app.jar"]
