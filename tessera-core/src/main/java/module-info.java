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

    uses com.quorum.tessera.transaction.publish.PayloadPublisherFactory;

    uses com.quorum.tessera.transaction.TransactionManagerFactory;

    uses com.quorum.tessera.transaction.publish.BatchPayloadPublisherFactory;

    uses com.quorum.tessera.transaction.EncodedPayloadManager;

    provides com.quorum.tessera.transaction.TransactionManagerFactory with
        com.quorum.tessera.transaction.TransactionManagerFactoryProvider;

}
