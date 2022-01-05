module tessera.transaction.jaxrs {
  requires jakarta.validation;
  requires jakarta.ws.rs;
  requires org.slf4j;
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
  requires jakarta.json;
  requires io.swagger.v3.oas.annotations;

  exports com.quorum.tessera.q2t;

  provides com.quorum.tessera.config.apps.TesseraApp with
      com.quorum.tessera.q2t.Q2TRestApp;
  provides com.quorum.tessera.transaction.publish.PayloadPublisher with
      com.quorum.tessera.q2t.internal.PayloadPublisherProvider;
  provides com.quorum.tessera.transaction.publish.BatchPayloadPublisher with
      com.quorum.tessera.q2t.internal.BatchPayloadPublisherProvider;
  provides com.quorum.tessera.privacygroup.publish.PrivacyGroupPublisher with
      com.quorum.tessera.q2t.internal.PrivacyGroupPublisherProvider;
  provides com.quorum.tessera.privacygroup.publish.BatchPrivacyGroupPublisher with
      com.quorum.tessera.q2t.internal.BatchPrivacyGroupPublisherProvider;
}
