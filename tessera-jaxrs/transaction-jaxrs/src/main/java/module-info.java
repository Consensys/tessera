module tessera.tessera.jaxrs.transaction.jaxrs.main {
  requires java.validation;
  requires java.ws.rs;
  requires org.slf4j;
  requires io.swagger.v3.oas.annotations;
  requires tessera.config.main;
  requires tessera.encryption.encryption.api.main;
  requires tessera.transaction;
  requires tessera.tessera.data.main;
  requires tessera.tessera.jaxrs.common.jaxrs.main;
  requires tessera.partyinfo;
  requires tessera.enclave.enclave.api.main;
  requires tessera.tessera.context.main;
  requires tessera.tessera.jaxrs.jaxrs.client.main;
  requires tessera.shared.main;
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
