# Build
FROM adoptopenjdk/openjdk11:alpine as builder

#do not use root as there are test cases validating file accessibility
ADD --chown=nobody:nogroup . /tessera
USER nobody:nogroup
RUN cd /tessera && ./gradlew -Dgradle.user.home=/tessera -Dmaven.repo.local=/tessera/.m2/repository -x test -x javadoc build

# Create docker image with only distribution jar
FROM adoptopenjdk/openjdk11:alpine

COPY --from=builder /tessera/tessera-dist/tessera-app/build/libs/*-app.jar /tessera/tessera-app.jar

ENTRYPOINT ["java", "-jar", "/tessera/tessera-app.jar"]
