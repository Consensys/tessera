# Extract pre-built .tar distribution
FROM alpine:3.13 as extractor

COPY tessera-*.tar /tessera/distributions/tessera.tar

RUN mkdir /tessera/distributions/extracted && tar xvf /tessera/distributions/tessera.tar -C /tessera/distributions/extracted --strip-components 1

# Create executable image
FROM adoptopenjdk/openjdk11:alpine

COPY --from=extractor /tessera/distributions/extracted /tessera

ENTRYPOINT ["/tessera/bin/tessera"]