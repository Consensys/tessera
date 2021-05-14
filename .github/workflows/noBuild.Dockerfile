# Create docker image with only distribution jar

FROM adoptopenjdk/openjdk11:alpine

COPY tessera-extracted /home/tessera-extracted

ENTRYPOINT ["/home/tessera-extracted/bin/tessera"]