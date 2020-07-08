module tessera.tessera.core.main {
    requires org.slf4j;
    requires tessera.config.main;
    requires tessera.enclave.enclave.api.main;
    requires tessera.encryption.encryption.api.main;
    requires tessera.shared.main;
    requires tessera.tessera.data.main;
    requires tessera.tessera.partyinfo.main;

    exports com.quorum.tessera.transaction;
    exports com.quorum.tessera.transaction.exception;
}
