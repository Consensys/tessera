module tessera.tessera.recover.main {
  requires tessera.config;
  requires tessera.data;
  requires tessera.partyinfo;
  requires tessera.enclave.enclave.api.main;
  requires tessera.shared.main;
  requires tessera.encryption.api;
  requires tessera.tessera.context.main;
  requires org.slf4j;
  requires tessera.transaction;
  requires java.persistence;

  exports com.quorum.tessera.recovery;
  exports com.quorum.tessera.recovery.resend;
  exports com.quorum.tessera.recovery.workflow;

  uses com.quorum.tessera.recovery.Recovery;
  uses com.quorum.tessera.recovery.workflow.BatchResendManager;
  uses com.quorum.tessera.recovery.workflow.BatchWorkflowFactory;
  uses com.quorum.tessera.recovery.resend.BatchTransactionRequester;
  uses com.quorum.tessera.recovery.resend.ResendBatchPublisher;
  uses com.quorum.tessera.recovery.workflow.LegacyResendManager;

  provides com.quorum.tessera.recovery.workflow.BatchResendManager with
      com.quorum.tessera.recovery.workflow.BatchResendManagerProvider;
  provides com.quorum.tessera.recovery.Recovery with
      com.quorum.tessera.recovery.RecoveryProvider;
  provides com.quorum.tessera.recovery.workflow.BatchWorkflowFactory with
      com.quorum.tessera.recovery.workflow.BatchWorkflowFactoryProvider;
  provides com.quorum.tessera.recovery.workflow.LegacyResendManager with
      com.quorum.tessera.recovery.workflow.LegacyResendManagerProvider;
}
