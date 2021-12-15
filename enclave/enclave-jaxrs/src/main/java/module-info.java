module tessera.enclave.jaxrs {
  requires jakarta.json;
  requires jakarta.ws.rs;
  requires jakarta.xml.bind;
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
  requires com.fasterxml.classmate;
  requires org.glassfish.json.jaxrs;
  requires org.eclipse.persistence.asm;

  opens com.quorum.tessera.enclave.rest to
      org.eclipse.persistence.moxy,
      org.eclipse.persistence.core;

  exports com.quorum.tessera.enclave.rest to
      org.eclipse.persistence.core,
      jersey.server,
      org.glassfish.hk2.locator;

  provides com.quorum.tessera.enclave.EnclaveClient with
      com.quorum.tessera.enclave.rest.EnclaveClientProvider;
}
