module tessera.thirdparty.jaxrs {
  requires jakarta.json;
  requires jakarta.ws.rs;
  requires tessera.config;
  requires tessera.shared;
  requires tessera.encryption.api;
  requires tessera.context;
  requires tessera.transaction;
  requires tessera.common.jaxrs;
  requires tessera.partyinfo;
  requires tessera.partyinfo.model;
  requires io.swagger.v3.oas.annotations;

  exports com.quorum.tessera.thirdparty;

  provides com.quorum.tessera.config.apps.TesseraApp with
      com.quorum.tessera.thirdparty.ThirdPartyRestApp;
}
