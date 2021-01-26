Feature: Hashicorp Vault support
    Storing and retrieving Tessera public/private key pairs from a Hashicorp Vault

    Background:
        Given the vault server has been started with TLS-enabled
        And the vault is initialised and unsealed
        And the vault has a v2 kv secret engine

    Scenario: Tessera retrieves a key pair from the Vault using the Token auth method
        Given the vault contains a key pair
        And the configfile contains the correct vault configuration
        And the configfile contains the correct key data
        When Tessera is started with the following CLI args and token environment variable
            """
            -configfile %s -pidfile %s -o jdbc.autoCreateTables=true
            """
        Then Tessera will retrieve the key pair from the vault

    Scenario: Tessera retrieves a key pair from the Vault using the default AppRole auth method without having to specify the default path
        Given the vault contains a key pair
        And the AppRole auth method is enabled at the default path
        And the configfile contains the correct vault configuration
        And the configfile contains the correct key data
        When Tessera is started with the following CLI args and approle environment variables
            """
            -configfile %s -pidfile %s -o jdbc.autoCreateTables=true
            """
        Then Tessera will retrieve the key pair from the vault

    Scenario: Tessera retrieves a key pair from the Vault using the custom AppRole auth method when specifying the custom path
        Given the vault contains a key pair
        And the AppRole auth method is enabled at a custom path
        And the configfile contains the correct vault configuration and custom approle configuration
        And the configfile contains the correct key data
        When Tessera is started with the following CLI args and approle environment variables
            """
            -configfile %s -pidfile %s -o jdbc.autoCreateTables=true
            """
        Then Tessera will retrieve the key pair from the vault

    Scenario: Tessera generates and stores a keypair in the Vault using the Token auth method
        When Tessera keygen is run with the following CLI args and token environment variable
            """
            -keygen -keygenvaulttype HASHICORP -keygenvaulturl https://localhost:8200 -keygenvaultsecretengine kv -filename tessera/nodeA,tessera/nodeB -keygenvaultkeystore %s -keygenvaulttruststore %s
            """
        Then a new key pair tessera/nodeA will have been added to the vault
        And a new key pair tessera/nodeB will have been added to the vault

    Scenario: Tessera generates and stores a keypair in the Vault using the default AppRole auth method without having to specify the default path
        Given the AppRole auth method is enabled at the default path
        When Tessera keygen is run with the following CLI args and approle environment variables
            """
            -keygen -keygenvaulttype HASHICORP -keygenvaulturl https://localhost:8200 -keygenvaultsecretengine kv -filename tessera/nodeA,tessera/nodeB -keygenvaultkeystore %s -keygenvaulttruststore %s
            """
        Then a new key pair tessera/nodeA will have been added to the vault
        And a new key pair tessera/nodeB will have been added to the vault

    Scenario: Tessera generates and stores a keypair in the Vault using the custom AppRole auth method when specifying the custom path
        Given the AppRole auth method is enabled at the custom path
        When Tessera keygen is run with the following CLI args and approle environment variables
            """
            -keygen -keygenvaulttype HASHICORP -keygenvaulturl https://localhost:8200 -keygenvaultsecretengine kv -filename tessera/nodeA,tessera/nodeB -keygenvaultkeystore %s -keygenvaulttruststore %s -keygenvaultapprole different-approle
            """
        Then a new key pair tessera/nodeA will have been added to the vault
        And a new key pair tessera/nodeB will have been added to the vault
