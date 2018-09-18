# Build
FROM maven:3.5.4-jdk-8 as builder

# do not use root as there are test cases validating file accessibility
USER nobody:nogroup
ADD --chown=nobody:nogroup . /tessera
RUN cd /tessera && mvn -Dmaven.repo.local=/tessera/.m2/repository package

# Create docker image with only distribution jar
FROM openjdk:8-jre-alpine

COPY --from=builder /tessera/tessera-app/target/*-app.jar /tessera/tessera-app.jar

ENTRYPOINT ["java", "-jar", "/tessera/tessera-app.jar"]