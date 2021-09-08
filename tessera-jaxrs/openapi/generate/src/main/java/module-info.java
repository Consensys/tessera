module tessera.openapi.generate {
  requires static tessera.common.jaxrs;
  requires static tessera.partyinfo.jaxrs;
  requires static tessera.transaction.jaxrs;
  requires static tessera.thirdparty.jaxrs;
  requires static tessera.openapi.common;
  requires static tessera.enclave.api;
  requires static tessera.partyinfo;
  requires static tessera.transaction;
  requires static tessera.shared;
  requires static tessera.partyinfo.model;
  requires static tessera.encryption.api;
  requires static tessera.config;
  requires static tessera.recovery;
  requires static jakarta.json;
}
