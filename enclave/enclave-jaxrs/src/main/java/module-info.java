module tessera.enclave.jaxrs {
  requires java.json;
  requires java.ws.rs;
  requires java.xml.bind;
  requires info.picocli;
  requires org.apache.commons.lang3;
  requires org.slf4j;
  requires tessera.cli.api;
  requires tessera.config;
  requires tessera.enclave.api;
  requires tessera.enclave.cli;
  requires tessera.encryption.api;
  requires tessera.server.jersey;
  requires tessera.server.api;
  requires tessera.shared;
  requires tessera.jaxrs.client;
  requires org.bouncycastle.provider;

  opens com.quorum.tessera.enclave.rest to
      org.eclipse.persistence.moxy,
      org.eclipse.persistence.core;

  exports com.quorum.tessera.enclave.rest to
      org.eclipse.persistence.core,
      hk2.locator,
      jersey.server;

  provides com.quorum.tessera.enclave.EnclaveClient with
      com.quorum.tessera.enclave.rest.EnclaveClientProvider;
}
