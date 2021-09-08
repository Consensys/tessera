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
  requires tessera.jaxrs.client;
  requires org.slf4j;
  requires io.swagger.v3.oas.annotations;

  exports com.quorum.tessera.thirdparty;
  exports com.quorum.tessera.thirdparty.messaging;

  opens com.quorum.tessera.thirdparty.messaging to
    org.eclipse.persistence.moxy;

  provides com.quorum.tessera.config.apps.TesseraApp with
    com.quorum.tessera.thirdparty.ThirdPartyRestApp;
  provides com.quorum.tessera.messaging.Courier with
    com.quorum.tessera.thirdparty.messaging.CourierProvider;
}
