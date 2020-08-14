module tessera.tessera.jaxrs.sync.jaxrs.main {
    requires java.json;
    requires java.validation;
    requires java.ws.rs;
    requires org.slf4j;
    requires swagger.annotations;
    requires tessera.config.main;
    requires tessera.enclave.enclave.api.main;
    requires tessera.encryption.encryption.api.main;
    requires tessera.security.main;
    requires tessera.shared.main;
    requires tessera.tessera.context.main;
    requires tessera.tessera.core.main;
    requires tessera.tessera.data.main;
    requires tessera.tessera.jaxrs.common.jaxrs.main;
    requires tessera.tessera.jaxrs.jaxrs.client.main;
    requires tessera.tessera.partyinfo.main;
    requires org.apache.commons.lang3;
    requires tessera.tessera.partyinfo.model;

    exports com.quorum.tessera.p2p;

    provides com.quorum.tessera.partyinfo.TransactionRequesterFactory with com.quorum.tessera.p2p.TransactionRequesterFactoryImpl;
    provides com.quorum.tessera.p2p.ResendClientFactory with com.quorum.tessera.p2p.RestResendClientFactory;
}
