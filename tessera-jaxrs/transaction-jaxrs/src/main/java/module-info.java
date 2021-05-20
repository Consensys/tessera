module tessera.transaction.jaxrs {
  requires java.validation;
  requires java.ws.rs;
  requires org.slf4j;
  requires io.swagger.v3.oas.annotations;
  requires tessera.config;
  requires tessera.encryption.api;
  requires tessera.transaction;
  requires tessera.data;
  requires tessera.common.jaxrs;
  requires tessera.partyinfo;
  requires tessera.enclave.api;
  requires tessera.context;
  requires tessera.jaxrs.client;
  requires tessera.shared;
  requires java.json;

  exports com.quorum.tessera.q2t;

  provides com.quorum.tessera.config.apps.TesseraApp with
      com.quorum.tessera.q2t.Q2TRestApp;
  provides com.quorum.tessera.transaction.publish.PayloadPublisher with
      com.quorum.tessera.q2t.PayloadPublisherProvider;
  provides com.quorum.tessera.transaction.publish.BatchPayloadPublisher with
      com.quorum.tessera.q2t.BatchPayloadPublisherProvider;
  provides com.quorum.tessera.privacygroup.publish.PrivacyGroupPublisher with
      com.quorum.tessera.q2t.PrivacyGroupPublisherProvider;
  provides com.quorum.tessera.privacygroup.publish.BatchPrivacyGroupPublisher with
      com.quorum.tessera.q2t.BatchPrivacyGroupPublisherProvider;
}
