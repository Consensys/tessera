module tessera.key.vault.aws.key.vault.main {
    requires software.amazon.awssdk.core;
    requires software.amazon.awssdk.services.secretsmanager;
    requires tessera.config.main;
    requires tessera.key.vault.key.vault.api.main;

    provides com.quorum.tessera.key.vault.KeyVaultServiceFactory with
        com.quorum.tessera.key.vault.aws.AWSKeyVaultServiceFactory;
}
