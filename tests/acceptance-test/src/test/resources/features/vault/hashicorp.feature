Feature: Hashicorp Vault support
    Storing and retrieving Tessera public/private key pairs from a Hashicorp Vault

    Background:
        Given the vault server has been started with TLS-enabled
        And the vault is initialised and unsealed
        And the vault has a v2 kv secret engine

    Scenario: A key pair stored in the vault is retrieved by Tessera on start up
        Given the vault contains a key pair
        And the configfile contains the correct vault configuration
        And the configfile contains the correct key data
        When Tessera is started with the following CLI args and token environment variable
            """
            -configfile %s -pidfile %s -jdbc.autoCreateTables true
            """
        Then Tessera will retrieve the key pair from the vault

    Scenario: Keygen using Token Vault auth method
        When Tessera keygen is run with the following CLI args and token environment variable
            """
            -keygen -keygenvaulttype HASHICORP -keygenvaulturl https://localhost:8200 -keygenvaultsecretengine secret -filename tessera/nodeA,tessera/nodeB -keygenvaultkeystore %s -keygenvaulttruststore %s
            """
        Then a new key pair tessera/nodeA will have been added to the vault
        And a new key pair tessera/nodeB will have been added to the vault

    Scenario: Keygen using Approle Vault auth method at default path
        Given the AppRole auth method is enabled at the default path
        When Tessera keygen is run with the following CLI args and approle environment variables
            """
            -keygen -keygenvaulttype HASHICORP -keygenvaulturl https://localhost:8200 -keygenvaultsecretengine secret -filename tessera/nodeA,tessera/nodeB -keygenvaultkeystore %s -keygenvaulttruststore %s
            """
        Then a new key pair tessera/nodeA will have been added to the vault
        And a new key pair tessera/nodeB will have been added to the vault

    Scenario: Keygen using Approle Vault auth method at non-default path
        Given the AppRole auth method is enabled at the custom different-approle path
        When Tessera keygen is run with the following CLI args and approle environment variables
            """
            -keygen -keygenvaulttype HASHICORP -keygenvaulturl https://localhost:8200 -keygenvaultsecretengine secret -filename tessera/nodeA,tessera/nodeB -keygenvaultkeystore %s -keygenvaulttruststore %s -keygenvaultapprole different-approle
            """
        Then a new key pair tessera/nodeA will have been added to the vault
        And a new key pair tessera/nodeB will have been added to the vault
