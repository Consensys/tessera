module tessera.key.vault.hashicorp.key.vault.main {
  requires spring.core;
  requires spring.vault.core;
  requires spring.web;
  requires tessera.config;
  requires tessera.key.vault.key.vault.api.main;

  provides com.quorum.tessera.key.vault.KeyVaultServiceFactory with
      com.quorum.tessera.key.vault.hashicorp.HashicorpKeyVaultServiceFactory;
}
