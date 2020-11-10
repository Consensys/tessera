module tessera.tessera.recover.main {
    requires tessera.config.main;
    requires tessera.tessera.data.main;
    requires tessera.tessera.partyinfo.main;
    requires tessera.enclave.enclave.api.main;
    requires tessera.shared.main;
    requires tessera.encryption.encryption.api.main;
    requires tessera.tessera.context.main;
    requires org.slf4j;
    requires tessera.tessera.core.main;
    requires java.persistence;

    exports com.quorum.tessera.recovery;
    exports com.quorum.tessera.recovery.resend;
    exports com.quorum.tessera.recovery.workflow;

    uses com.quorum.tessera.recovery.RecoveryFactory;
    uses com.quorum.tessera.recovery.resend.ResendBatchPublisherFactory;
    uses com.quorum.tessera.recovery.workflow.BatchResendManager;
    uses com.quorum.tessera.recovery.workflow.BatchWorkflowFactory;
    uses com.quorum.tessera.recovery.resend.BatchTransactionRequesterFactory;

    provides com.quorum.tessera.recovery.RecoveryFactory with
        com.quorum.tessera.recovery.RecoveryFactoryImpl;


}
