module tessera.jaxrs.client {
  requires jakarta.ws.rs;
  requires tessera.config;
  requires tessera.security;
  requires tessera.shared;
  requires tessera.context;

  exports com.quorum.tessera.jaxrs.client;

  provides com.quorum.tessera.context.RestClientFactory with
      com.quorum.tessera.jaxrs.client.ClientFactory;

  uses com.quorum.tessera.ssl.context.SSLContextFactory;
  uses com.quorum.tessera.ssl.context.ClientSSLContextFactory;
}
