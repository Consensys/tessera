module tessera.keyvault.api {
  requires tessera.config;
  requires tessera.shared;

  uses com.quorum.tessera.key.vault.KeyVaultServiceFactory;

  exports com.quorum.tessera.key.vault;
}
