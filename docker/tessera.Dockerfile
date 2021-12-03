# Create Tessera installation from pre-built .tar distributions
FROM alpine:3.13 as extractor

COPY . /ctx/

# check all necessary files are present
RUN if [ -z $(find . -name tessera-*.tar 2>/dev/null) ] ; then echo "ERROR: No tessera distribution in Docker context" && exit 1; fi

# install dists
RUN mkdir /install
RUN mkdir /install/tessera && tar xvf $(find . -name tessera-*.tar 2>/dev/null) -C /install/tessera --strip-components 1

# Create executable image
FROM adoptopenjdk/openjdk11:alpine

COPY --from=extractor /install/tessera/ /tessera

ENTRYPOINT ["/tessera/bin/tessera"]