module tessera.keyvault.azure {
  requires jdk.unsupported;
  requires org.slf4j;
  requires tessera.config;
  requires tessera.keyvault.api;
  requires com.azure.identity;
  requires com.azure.security.keyvault.secrets;
  requires kotlin.stdlib;

  provides com.quorum.tessera.key.vault.KeyVaultServiceFactory with
      com.quorum.tessera.key.vault.azure.AzureKeyVaultServiceFactory;
}
