module tessera.keyvault.aws {
  requires software.amazon.awssdk.core;
  requires software.amazon.awssdk.services.secretsmanager;
  requires tessera.config;
  requires tessera.keyvault.api;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.databind;
  requires org.slf4j;
  requires org.apache.commons.logging;

  provides com.quorum.tessera.key.vault.KeyVaultServiceFactory with
      com.quorum.tessera.key.vault.aws.AWSKeyVaultServiceFactory;
}
