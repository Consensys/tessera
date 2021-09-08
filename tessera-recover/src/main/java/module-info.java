module tessera.recovery {
  requires tessera.config;
  requires tessera.data;
  requires tessera.partyinfo;
  requires tessera.enclave.api;
  requires tessera.shared;
  requires tessera.encryption.api;
  requires tessera.context;
  requires org.slf4j;
  requires tessera.transaction;
  requires jakarta.persistence;

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
      com.quorum.tessera.recovery.workflow.internal.BatchResendManagerProvider;
  provides com.quorum.tessera.recovery.Recovery with
      com.quorum.tessera.recovery.internal.RecoveryProvider;
  provides com.quorum.tessera.recovery.workflow.BatchWorkflowFactory with
      com.quorum.tessera.recovery.workflow.internal.BatchWorkflowFactoryProvider;
  provides com.quorum.tessera.recovery.workflow.LegacyResendManager with
      com.quorum.tessera.recovery.workflow.internal.LegacyResendManagerProvider;
}
