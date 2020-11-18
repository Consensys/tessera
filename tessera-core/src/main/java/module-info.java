module tessera.tessera.core.main {

    requires org.slf4j;
    requires tessera.config.main;
    requires tessera.enclave.enclave.api.main;
    requires tessera.encryption.encryption.api.main;
    requires tessera.shared.main;
    requires tessera.tessera.data.main;
    requires tessera.tessera.partyinfo.main;
    requires tessera.tessera.context.main;

    exports com.quorum.tessera.transaction;
    exports com.quorum.tessera.transaction.exception;
    exports com.quorum.tessera.transaction.publish;

    uses com.quorum.tessera.transaction.publish.PayloadPublisher;

    uses com.quorum.tessera.transaction.TransactionManager;

    uses com.quorum.tessera.transaction.publish.BatchPayloadPublisher;

    uses com.quorum.tessera.transaction.EncodedPayloadManager;

    uses com.quorum.tessera.transaction.resend.ResendManager;

    uses com.quorum.tessera.transaction.PrivacyHelper;

    provides com.quorum.tessera.transaction.TransactionManager with
        com.quorum.tessera.transaction.TransactionManagerProvider;

    provides com.quorum.tessera.transaction.EncodedPayloadManager with
        com.quorum.tessera.transaction.EncodedPayloadManagerProvider;

    provides com.quorum.tessera.transaction.PrivacyHelper with
        com.quorum.tessera.transaction.PrivacyHelperProvider;

    provides com.quorum.tessera.transaction.resend.ResendManager
        with com.quorum.tessera.transaction.resend.ResendManagerProvider;




}
