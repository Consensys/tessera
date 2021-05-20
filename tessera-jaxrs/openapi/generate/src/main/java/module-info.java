module tessera.tessera.jaxrs.openapi.generate.main {
  requires static tessera.common.jaxrs;
  requires static tessera.partyinfo.jaxrs;
  requires static tessera.transaction.jaxrs;
  requires static tessera.thirdparty.jaxrs;
  requires static tessera.tessera.jaxrs.openapi.common.main;
  requires static tessera.enclave.enclave.api.main;
  requires static tessera.partyinfo;
  requires static tessera.transaction;
  requires static tessera.shared.main;
  requires static tessera.partyinfo.model;
  requires static tessera.encryption.api;
  requires static tessera.config;
  requires static tessera.recovery;
  requires static java.json;
}
