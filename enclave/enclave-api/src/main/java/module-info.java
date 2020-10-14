module tessera.enclave.enclave.api.main {
    requires tessera.config.main;
    requires tessera.encryption.encryption.api.main;
    requires tessera.key.vault.key.vault.api.main;
    requires tessera.shared.main;
    requires org.bouncycastle.provider;
    requires org.slf4j;

    exports com.quorum.tessera.enclave;
}
