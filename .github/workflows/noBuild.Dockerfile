# Create docker image with only distribution jar

FROM adoptopenjdk/openjdk11:alpine

ARG DIST
COPY ${DIST} /tessera-dist

ENTRYPOINT ["/tessera-dist/bin/tessera"]
