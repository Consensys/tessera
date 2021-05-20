module tessera.tessera.jaxrs.openapi.generate.main {
  requires static tessera.tessera.jaxrs.common.jaxrs.main;
  requires static tessera.tessera.jaxrs.sync.jaxrs.main;
  requires static tessera.tessera.jaxrs.transaction.jaxrs.main;
  requires static tessera.tessera.jaxrs.thirdparty.jaxrs.main;
  requires static tessera.tessera.jaxrs.openapi.common.main;
  requires static tessera.enclave.enclave.api.main;
  requires static tessera.partyinfo;
  requires static tessera.transaction;
  requires static tessera.shared.main;
  requires static tessera.tessera.partyinfo.model;
  requires static tessera.encryption.encryption.api.main;
  requires static tessera.config;
  requires static tessera.tessera.recover.main;
  requires static java.json;
}
