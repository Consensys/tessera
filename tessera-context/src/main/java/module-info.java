module tessera.context {
  requires jakarta.validation;
  requires jakarta.xml.bind;
  requires jakarta.ws.rs;
  requires org.slf4j;
  requires tessera.config;
  requires tessera.enclave.api;
  requires tessera.encryption.api;
  requires tessera.shared;

  exports com.quorum.tessera.context;

  uses com.quorum.tessera.context.KeyVaultConfigValidations;
  uses com.quorum.tessera.context.RestClientFactory;
  uses com.quorum.tessera.context.RuntimeContext;

  provides com.quorum.tessera.context.KeyVaultConfigValidations with
      com.quorum.tessera.context.internal.DefaultKeyVaultConfigValidations;
  provides com.quorum.tessera.context.RuntimeContext with
      com.quorum.tessera.context.internal.RuntimeContextProvider;
}
