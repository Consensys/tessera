Feature: Aws Key Vault support
    Storing and retrieving Tessera public/private key pairs from an AWS Secrets Manager

    Background:
        Given the mock AWS Secrets Manager server has been started

    Scenario: Tessera authenticates with AWS Secrets Manager and retrieves a key pair
        Given the mock AWS Secrets Manager server has stubs for the endpoints used to get secrets
        When Tessera is started with the correct AWS Secrets Manager environment variables
        Then Tessera will retrieve the key pair from AWS Secrets Manager

    Scenario: Tessera generates and stores multiple keypairs in AWS Secrets Manager
        Given the mock AWS Secrets Manager server has stubs for the endpoints used to store secrets
        When Tessera keygen is run with the following CLI args and AWS Secrets Manager environment variables
            """
            -keygen -keygenvaulttype AWS -filename nodeA -keygenvaulturl %s
            """
        Then key pairs nodeA and nodeB will have been added to the AWS Secrets Manager
