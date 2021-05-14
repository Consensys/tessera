# Create docker image with only distribution jar

FROM adoptopenjdk/openjdk11:alpine

COPY --from=builder /tessera-extracted /tessera-dist

ENTRYPOINT ["/tessera-dist/bin/tessera"]
