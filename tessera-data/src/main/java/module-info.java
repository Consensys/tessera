module tessera.tessera.data.main {
    requires java.persistence;
    requires org.bouncycastle.provider;
    requires org.slf4j;
    requires tessera.config.main;
    requires tessera.enclave.enclave.api.main;
    requires tessera.encryption.encryption.api.main;
    requires tessera.shared.main;

    exports com.quorum.tessera.data;
}
