# Build
FROM maven:3.5.4-jdk-8 as builder

# do not use root as there are test cases validating file accessibility
USER nobody:nogroup
ADD --chown=nobody:nogroup . /tessera
RUN cd /tessera && mvn clean -Dmaven.repo.local=/tessera/.m2/repository -DskipTests -Denforcer.skip=true package

# Create docker image with only distribution jar
FROM openjdk:8-jre-alpine

COPY --from=builder /tessera/tessera-dist/tessera-app/target/*-app.jar /tessera/tessera-app.jar

ENTRYPOINT ["java", "-jar", "/tessera/tessera-app.jar"]
