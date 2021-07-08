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
  requires tessera.jaxrs.client;

  exports com.quorum.tessera.thirdparty;
  exports com.quorum.tessera.thirdparty.messaging;

  provides com.quorum.tessera.config.apps.TesseraApp with
      com.quorum.tessera.thirdparty.ThirdPartyRestApp;
  provides com.quorum.tessera.messaging.Courier with
      com.quorum.tessera.thirdparty.messaging.CourierProvider;
}
