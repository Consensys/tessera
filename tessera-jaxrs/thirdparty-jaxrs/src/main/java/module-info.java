module tessera.thirdparty.jaxrs {
  requires java.json;
  requires java.ws.rs;
  requires io.swagger.v3.oas.annotations;
  requires tessera.config;
  requires tessera.shared;
  requires tessera.encryption.api;
  requires tessera.context;
  requires tessera.transaction;
  requires tessera.common.jaxrs;
  requires tessera.partyinfo;
  requires tessera.partyinfo.model;

  exports com.quorum.tessera.thirdparty;

  provides com.quorum.tessera.config.apps.TesseraApp with
      com.quorum.tessera.thirdparty.ThirdPartyRestApp;
}
