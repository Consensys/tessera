module tessera.tessera.jaxrs.sync.jaxrs.main {
    requires java.json;
    requires java.validation;
    requires java.ws.rs;
    requires org.slf4j;
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
    requires tessera.tessera.recover.main;
    requires swagger.annotations;

    exports com.quorum.tessera.p2p;
    exports com.quorum.tessera.p2p.resend;
    exports com.quorum.tessera.p2p.partyinfo;
    exports com.quorum.tessera.p2p.recovery;

    uses com.quorum.tessera.p2p.partyinfo.PartyStore;
    uses com.quorum.tessera.p2p.recovery.RecoveryClientFactory;
    uses com.quorum.tessera.p2p.resend.ResendClientFactory;

    opens com.quorum.tessera.p2p.resend to org.eclipse.persistence.moxy,org.hibernate.validator,org.eclipse.persistence.core;

    provides com.quorum.tessera.p2p.resend.TransactionRequesterFactory with com.quorum.tessera.p2p.resend.TransactionRequesterFactoryImpl;
    provides com.quorum.tessera.p2p.resend.ResendClientFactory with com.quorum.tessera.p2p.resend.RestResendClientFactory;

    provides com.quorum.tessera.config.apps.TesseraApp with com.quorum.tessera.p2p.P2PRestApp;

    provides com.quorum.tessera.p2p.partyinfo.PartyStore with com.quorum.tessera.p2p.partyinfo.PartyStoreFactory;

    provides com.quorum.tessera.p2p.recovery.RecoveryClientFactory with com.quorum.tessera.p2p.recovery.RestRecoveryClientFactory;

    provides com.quorum.tessera.partyinfo.P2pClientFactory with com.quorum.tessera.p2p.partyinfo.RestP2pClientFactory;

    provides com.quorum.tessera.recovery.resend.BatchTransactionRequesterFactory with com.quorum.tessera.p2p.recovery.RestBatchTransactionRequesterFactory;

    provides com.quorum.tessera.recovery.resend.ResendBatchPublisherFactory with com.quorum.tessera.p2p.recovery.RestResendBatchPublisherFactory;

//    provides com.quorum.tessera.transaction.publish.PayloadPublisherFactory with
//        com.quorum.tessera.q2t.RestPayloadPublisherFactory;

    provides com.quorum.tessera.api.Version with com.quorum.tessera.p2p.Version;


}
