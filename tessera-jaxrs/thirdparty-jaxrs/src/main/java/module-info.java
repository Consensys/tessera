module tessera.tessera.jaxrs.thirdparty.jaxrs.main {
  requires java.json;
  requires java.ws.rs;
  requires io.swagger.v3.oas.annotations;
  requires tessera.config;
  requires tessera.shared.main;
  requires tessera.encryption.encryption.api.main;
  requires tessera.tessera.context.main;
  requires tessera.transaction;
  requires tessera.tessera.jaxrs.common.jaxrs.main;
  requires tessera.partyinfo;
  requires tessera.tessera.partyinfo.model;

  exports com.quorum.tessera.thirdparty;

  provides com.quorum.tessera.config.apps.TesseraApp with
      com.quorum.tessera.thirdparty.ThirdPartyRestApp;
}
