module tessera.tests.acceptance.test.main {
  requires org.slf4j;
  requires java.sql;
  requires tessera.encryption.encryption.jnacl.main;
  requires tessera.security.main;
  requires tessera.config;
  requires tessera.encryption.encryption.api.main;
  requires java.ws.rs;
  requires tessera.partyinfo.jaxrs;
  requires tessera.jaxrs.client;
  requires tessera.enclave.enclave.api.main;
  requires tessera.common.jaxrs;
  requires tessera.partyinfo.model;
  requires tessera.tessera.dist.main;
  requires tessera.shared.main;
  requires jdk.httpserver;
  requires java.net.http;
}
