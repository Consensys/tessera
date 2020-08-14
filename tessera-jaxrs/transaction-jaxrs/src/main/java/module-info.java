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

    provides com.quorum.tessera.transaction.publish.PayloadPublisherFactory
        with com.quorum.tessera.q2t.RestPayloadPublisherFactory;
}
