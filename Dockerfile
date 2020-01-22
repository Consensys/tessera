# Build
FROM maven:3.6.3-jdk-11 as builder

#do not use root as there are test cases validating file accessibility
USER nobody:nogroup
ADD --chown=nobody:nogroup . /tessera
RUN cd /tessera && mvn clean -Dmaven.repo.local=/tessera/.m2/repository -DskipTests -Denforcer.skip=true package

# Create docker image with only distribution jar

FROM alpine:latest
# Update bzip2 version to 1.0.8-r1 as the bundled version 1.0.6 has critical vulnerability https://nvd.nist.gov/vuln/detail/CVE-2019-12900
RUN apk add bzip2=1.0.8-r1 --update-cache --repository http://dl-cdn.alpinelinux.org/alpine/edge/main/ --allow-untrusted

FROM openjdk:11

COPY --from=builder /tessera/tessera-dist/tessera-app/target/*-app.jar /tessera/tessera-app.jar

ENTRYPOINT ["java", "-jar", "/tessera/tessera-app.jar"]
