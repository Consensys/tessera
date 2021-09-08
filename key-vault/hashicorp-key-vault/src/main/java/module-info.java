module tessera.keyvault.hashicorp {
  requires spring.core;
  requires spring.vault.core;
  requires spring.web;
  requires tessera.config;
  requires tessera.keyvault.api;

  provides com.quorum.tessera.key.vault.KeyVaultServiceFactory with
      com.quorum.tessera.key.vault.hashicorp.HashicorpKeyVaultServiceFactory;
}
