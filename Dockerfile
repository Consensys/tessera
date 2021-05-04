# Build
FROM maven:3.6.3-jdk-11 as builder

#do not use root as there are test cases validating file accessibility
USER nobody:nogroup
ADD --chown=nobody:nogroup . /tessera
RUN cd /tessera && ./gradlew -Dmaven.repo.local=/tessera/.m2/repository -x test -x javadoc build

# Create docker image with only distribution jar
FROM adoptopenjdk/openjdk11:alpine

COPY --from=builder /tessera/tessera-dist/tessera-app/build/libs/*-app.jar /tessera/tessera-app.jar

ENTRYPOINT ["java", "-jar", "/tessera/tessera-app.jar"]
