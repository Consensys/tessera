module tessera.keyvault.api {
  requires tessera.config;
  requires tessera.shared.main;

  uses com.quorum.tessera.key.vault.KeyVaultServiceFactory;

  exports com.quorum.tessera.key.vault;
}
