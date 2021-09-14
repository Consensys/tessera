module tessera.acceptance.tests {
  requires org.slf4j;
  requires java.sql;
  requires tessera.encryption.jnacl;
  requires tessera.security;
  requires tessera.config;
  requires tessera.encryption.api;
  requires jakarta.ws.rs;
  requires tessera.partyinfo.jaxrs;
  requires tessera.jaxrs.client;
  requires tessera.enclave.api;
  requires tessera.common.jaxrs;
  requires tessera.partyinfo.model;
  requires tessera.application;
  requires tessera.shared;
  requires tessera.data;
  requires jdk.httpserver;
  requires java.net.http;
  requires jakarta.json;
}
