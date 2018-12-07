Feature: Hashicorp Vault support
    Storing and retrieving Tessera public/private key pairs from a Hashicorp Vault

    Scenario: A key pair stored in the vault is retrieved by Tessera on start up
        Given the dev vault server has been started
        And the vault is initialised and unsealed
        And the v1 kv secret engine is enabled
        And the vault contains a key pair
        And the configfile contains the correct vault configuration
        And the configfile contains the correct key data
        When Tessera is started
        Then Tessera will have retrieved the key pair from the vault
