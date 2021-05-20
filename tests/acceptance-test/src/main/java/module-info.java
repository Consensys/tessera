module tessera.tests.acceptance.test.main {
  requires org.slf4j;
  requires java.sql;
  requires tessera.encryption.encryption.jnacl.main;
  requires tessera.security.main;
  requires tessera.config;
  requires tessera.encryption.encryption.api.main;
  requires java.ws.rs;
  requires tessera.tessera.jaxrs.sync.jaxrs.main;
  requires tessera.tessera.jaxrs.jaxrs.client.main;
  requires tessera.enclave.enclave.api.main;
  requires tessera.tessera.jaxrs.common.jaxrs.main;
  requires tessera.tessera.partyinfo.model;
  requires tessera.tessera.dist.main;
  requires tessera.shared.main;
  requires jdk.httpserver;
  requires java.net.http;
}
