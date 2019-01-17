Feature: Hashicorp Vault support
    Storing and retrieving Tessera public/private key pairs from a Hashicorp Vault

    Scenario: A key pair stored in the vault is retrieved by Tessera on start up
        Given the vault server has been started with TLS-enabled
        And the vault is initialised and unsealed
        And the vault has a v2 kv secret engine
        And the vault contains a key pair
        And the configfile contains the correct vault configuration
        And the configfile contains the correct key data
        When Tessera is started
        Then Tessera will retrieve the key pair from the vault

    Scenario: Keygen using Token Vault auth method
        Given the vault server has been started with TLS-enabled
        And the vault is initialised and unsealed
        And the vault has a v2 kv secret engine
        When Tessera keygen is used with the Hashicorp options provided and "token" auth method at path ""
        Then a new key pair "tessera/nodeA" will be added to the vault
        And a new key pair "tessera/nodeB" will be added to the vault

    Scenario: Keygen using Approle Vault auth method at default path
        Given the vault server has been started with TLS-enabled
        And the vault is initialised and unsealed
        And the vault has a v2 kv secret engine
        And the AppRole auth method is enabled at path "approle"
        When Tessera keygen is used with the Hashicorp options provided and "approle" auth method at path ""
        Then a new key pair "tessera/nodeA" will be added to the vault
        And a new key pair "tessera/nodeB" will be added to the vault

    Scenario: Keygen using Approle Vault auth method at non-default path
        Given the vault server has been started with TLS-enabled
        And the vault is initialised and unsealed
        And the vault has a v2 kv secret engine
        And the AppRole auth method is enabled at path "different-approle"
        When Tessera keygen is used with the Hashicorp options provided and "approle" auth method at path "different-approle"
        Then a new key pair "tessera/nodeA" will be added to the vault
        And a new key pair "tessera/nodeB" will be added to the vault
