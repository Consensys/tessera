module tessera.keyvault.hashicorp {
  requires spring.core;
  requires spring.vault.core;
  requires spring.web;
  requires tessera.config;
  requires tessera.keyvault.api;
  requires org.slf4j;
  requires org.apache.commons.logging;
  requires com.fasterxml.jackson.core;

  provides com.quorum.tessera.key.vault.KeyVaultServiceFactory with
      com.quorum.tessera.key.vault.hashicorp.HashicorpKeyVaultServiceFactory;
}
