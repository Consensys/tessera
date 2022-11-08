# Create Tessera Enclave installation from pre-built .tar distributions
FROM alpine:3.13 as extractor

COPY . /ctx/

# check all necessary files are present
RUN if [ -z $(find . -name enclave-jaxrs-*.tar 2>/dev/null) ] ; then echo "ERROR: No enclave-jaxrs distribution in Docker context" && exit 1; fi

# install dists
RUN mkdir /install
RUN mkdir /install/enclave-jaxrs && tar xvf $(find . -name enclave-jaxrs-*.tar 2>/dev/null) -C /install/enclave-jaxrs --strip-components 1

# Create executable image
FROM eclipse-temurin:17-jre-jammy

RUN apt-get update && apt-get install -y \
  wget \
  netcat \
  && rm -rf /var/lib/apt/lists/*

COPY --from=extractor /install/enclave-jaxrs/ /tessera

ENTRYPOINT ["/tessera/bin/enclave-jaxrs"]
