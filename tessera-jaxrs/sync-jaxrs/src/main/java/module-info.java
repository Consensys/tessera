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

    requires io.swagger.v3.oas.annotations;

    exports com.quorum.tessera.p2p;
    exports com.quorum.tessera.p2p.resend;
    exports com.quorum.tessera.p2p.partyinfo;
    exports com.quorum.tessera.p2p.recovery;

    uses com.quorum.tessera.p2p.recovery.RecoveryClient;
    uses com.quorum.tessera.p2p.resend.ResendClient;
    uses com.quorum.tessera.p2p.resend.TransactionRequester;

    opens com.quorum.tessera.p2p.resend to org.eclipse.persistence.moxy,org.hibernate.validator,org.eclipse.persistence.core;


    provides com.quorum.tessera.config.apps.TesseraApp with com.quorum.tessera.p2p.P2PRestApp;
    
    provides com.quorum.tessera.recovery.resend.BatchTransactionRequester with
        com.quorum.tessera.p2p.recovery.BatchTransactionRequesterProvider;

    provides com.quorum.tessera.recovery.resend.ResendBatchPublisher with
        com.quorum.tessera.p2p.recovery.ResendBatchPublisherProvider;

    provides com.quorum.tessera.p2p.resend.ResendClient with
            com.quorum.tessera.p2p.resend.ResendClientProvider;

    provides com.quorum.tessera.p2p.recovery.RecoveryClient with
         com.quorum.tessera.p2p.recovery.RecoveryClientProvider;

    provides com.quorum.tessera.p2p.resend.TransactionRequester
        with com.quorum.tessera.p2p.resend.TransactionRequesterProvider;

    provides com.quorum.tessera.partyinfo.P2pClient
        with com.quorum.tessera.p2p.partyinfo.P2pClientProvider;



}
