Feature: Azure Key Vault support
    Storing and retrieving Tessera public/private key pairs from an Azure Key Vault

    Background:
        Given the mock AKV server has been started

    Scenario: Tessera authenticates with AKV and retrieves a key pair
        Given the mock AKV server has stubs for the endpoints used to get secrets
        When Tessera is started with the correct AKV environment variables
        Then Tessera will retrieve the key pair from AKV

#    Scenario: Tessera generates and stores multiple keypairs in AKV
#        Given the mock AKV server has stubs for the endpoints used to store secrets
#        When Tessera keygen is run with the following CLI args and AKV environment variables
#            """
#            -keygen -keygenvaulttype AZURE -keygenvaulturl %s -filename nodeA,nodeB
#            """
#        Then key pairs nodeA and nodeB will have been added to the AKV
