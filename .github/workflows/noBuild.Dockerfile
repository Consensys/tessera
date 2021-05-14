# Create docker image with only distribution jar

FROM adoptopenjdk/openjdk11:alpine

ENTRYPOINT ["/tessera-extracted/bin/tessera"]
