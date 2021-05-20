module tessera.key.vault.azure.key.vault.main {
  requires jdk.unsupported;
  requires org.slf4j;
  requires tessera.config;
  requires tessera.key.vault.key.vault.api.main;
  requires java.annotation;
  requires com.azure.identity;
  requires com.azure.security.keyvault.secrets;
  requires com.azure.http.netty;
  requires reactor.netty;
  requires io.netty.handler;
  requires io.netty.common;

  provides com.quorum.tessera.key.vault.KeyVaultServiceFactory with
      com.quorum.tessera.key.vault.azure.AzureKeyVaultServiceFactory;
}
