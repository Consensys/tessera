# Create Tessera installation with Azure Key Vault support from pre-built .tar distributions
FROM alpine:3.13 as extractor

COPY . /ctx/

# check all necessary files are present
RUN if [ -z $(find . -name tessera-*.tar 2>/dev/null) ] ; then echo "ERROR: No tessera distribution in Docker context" && exit 1; fi
RUN if [ -z $(find . -name azure-key-vault-*.tar 2>/dev/null) ] ; then echo "ERROR: No azure-key-vault distribution in Docker context" && exit 1; fi

# install dists
RUN mkdir /install
RUN mkdir /install/tessera && tar xvf $(find . -name tessera-*.tar 2>/dev/null) -C /install/tessera --strip-components 1
RUN mkdir /install/azure-key-vault && tar xvf $(find . -name azure-key-vault-*.tar 2>/dev/null) -C /install/azure-key-vault --strip-components 1

# create tessera+vault dist (source paths need '/.' to copy only directory contents)
RUN mkdir /install/tessera-plus-vault && cp -a /install/azure-key-vault/. /install/tessera-plus-vault/ && cp -a /install/tessera/. /install/tessera-plus-vault/

# Create executable image
FROM adoptopenjdk/openjdk11:alpine

COPY --from=extractor /install/tessera-plus-vault/ /tessera

ENTRYPOINT ["/tessera/bin/tessera"]