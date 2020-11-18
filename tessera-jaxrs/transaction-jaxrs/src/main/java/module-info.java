module tessera.tessera.jaxrs.transaction.jaxrs.main {
    requires java.validation;
    requires java.ws.rs;
    requires org.slf4j;
    requires swagger.annotations;
    requires tessera.config.main;
    requires tessera.encryption.encryption.api.main;
    requires tessera.tessera.core.main;
    requires tessera.tessera.data.main;
    requires tessera.tessera.jaxrs.common.jaxrs.main;
    requires tessera.tessera.partyinfo.main;
    requires tessera.enclave.enclave.api.main;
    requires tessera.tessera.context.main;
    requires tessera.tessera.jaxrs.jaxrs.client.main;
    requires tessera.shared.main;


    exports com.quorum.tessera.q2t;

    provides com.quorum.tessera.config.apps.TesseraApp with
        com.quorum.tessera.q2t.Q2TRestApp;

    provides com.quorum.tessera.api.Version with com.quorum.tessera.q2t.Version;

    provides com.quorum.tessera.transaction.publish.PayloadPublisher with
        com.quorum.tessera.q2t.PayloadPublisherProvider;

    provides com.quorum.tessera.transaction.publish.BatchPayloadPublisher with
        com.quorum.tessera.q2t.BatchPayloadPublisherProvider;
}
