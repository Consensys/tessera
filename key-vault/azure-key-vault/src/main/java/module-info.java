module tessera.keyvault.azure {
  requires jdk.unsupported;
  requires org.slf4j;
  requires tessera.config;
  requires tessera.keyvault.api;
  //    requires jakarta.annotation;
  requires com.azure.identity;
  requires com.azure.security.keyvault.secrets;
  //  requires com.azure.http.okhttp;
  //  requires com.azure.http.netty;
  //  requires reactor.netty;
  //  requires io.netty.handler;
  //  requires io.netty.common;
  //  requires okio;

  requires kotlin.stdlib;

  provides com.quorum.tessera.key.vault.KeyVaultServiceFactory with
      com.quorum.tessera.key.vault.azure.AzureKeyVaultServiceFactory;
}
