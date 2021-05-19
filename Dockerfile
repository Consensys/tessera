# Build
FROM adoptopenjdk/openjdk11:alpine as builder

COPY . /tessera
RUN cd /tessera && ./gradlew build -x test -x dependencyCheckAnalyze -x javadoc
RUN cp /tessera/tessera-dist/build/distributions/tessera-*.tar /tessera/tessera-dist/build/distributions/tessera.tar
RUN mkdir /tessera-extracted && tar xvf /tessera/tessera-dist/build/distributions/tessera.tar -C /tessera-extracted --strip-components 1

# Create docker image with only distribution jar
FROM adoptopenjdk/openjdk11:alpine

COPY --from=builder /tessera-extracted /home/tessera-extracted

ENTRYPOINT ["/home/tessera-extracted/bin/tessera"]
