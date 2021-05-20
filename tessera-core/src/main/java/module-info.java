module tessera.transaction {
  requires org.slf4j;
  requires tessera.config;
  requires tessera.enclave.enclave.api.main;
  requires tessera.encryption.encryption.api.main;
  requires tessera.shared.main;
  requires tessera.tessera.data.main;
  requires tessera.partyinfo;
  requires tessera.tessera.context.main;

  exports com.quorum.tessera.transaction;
  exports com.quorum.tessera.transaction.exception;
  exports com.quorum.tessera.transaction.publish;
  exports com.quorum.tessera.privacygroup;
  exports com.quorum.tessera.privacygroup.exception;
  exports com.quorum.tessera.privacygroup.publish;

  uses com.quorum.tessera.transaction.publish.PayloadPublisher;
  uses com.quorum.tessera.transaction.TransactionManager;
  uses com.quorum.tessera.transaction.publish.BatchPayloadPublisher;
  uses com.quorum.tessera.transaction.EncodedPayloadManager;
  uses com.quorum.tessera.transaction.resend.ResendManager;
  uses com.quorum.tessera.transaction.PrivacyHelper;
  uses com.quorum.tessera.privacygroup.PrivacyGroupManager;
  uses com.quorum.tessera.privacygroup.publish.BatchPrivacyGroupPublisher;
  uses com.quorum.tessera.privacygroup.publish.PrivacyGroupPublisher;

  provides com.quorum.tessera.transaction.TransactionManager with
      com.quorum.tessera.transaction.TransactionManagerProvider;
  provides com.quorum.tessera.transaction.EncodedPayloadManager with
      com.quorum.tessera.transaction.EncodedPayloadManagerProvider;
  provides com.quorum.tessera.transaction.PrivacyHelper with
      com.quorum.tessera.transaction.PrivacyHelperProvider;
  provides com.quorum.tessera.transaction.resend.ResendManager with
      com.quorum.tessera.transaction.resend.ResendManagerProvider;
  provides com.quorum.tessera.privacygroup.PrivacyGroupManager with
      com.quorum.tessera.privacygroup.PrivacyGroupManagerProvider;
  provides com.quorum.tessera.privacygroup.ResidentGroupHandler with
      com.quorum.tessera.privacygroup.ResidentGroupHandlerProvider;
}
