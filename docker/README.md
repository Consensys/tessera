# Docker build files

The Docker build files allow for several Tessera image variations to be built, including:

* [`tessera.Dockerfile`](tessera.Dockerfile) - Core Tessera distribution
* [`tessera.azure.Dockerfile`](tessera.azure.Dockerfile) - Core Tessera distribution + Azure Key Vault support 
* [`tessera.aws.Dockerfile`](tessera.aws.Dockerfile) - Core Tessera distribution + AWS Key Vault support
* [`tessera.hashicorp.Dockerfile`](tessera.hashicorp.Dockerfile) - Core Tessera distribution + Hashicorp Key Vault support 

## Usage

From project root:

1. Build Tessera distributions, e.g.:
    ```shell
    ./gradlew clean build -x test
    ```

2. Build image, e.g.:
    ```shell
    docker build -f docker/tessera.Dockerfile -t me/tessera:develop .
    ```

3. Verify image, e.g.:
    ```shell
    docker run me/tessera:develop help
    ```

## .dockerignore

[.dockerignore](../.dockerignore) controls the files passed to the Docker build context. 
