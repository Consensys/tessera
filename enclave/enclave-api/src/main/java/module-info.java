module tessera.enclave.enclave.api.main {
    requires tessera.config.main;
    requires tessera.encryption.encryption.api.main;
    requires tessera.key.vault.key.vault.api.main;
    requires tessera.shared.main;
    requires org.bouncycastle.provider;
    requires org.slf4j;

    exports com.quorum.tessera.enclave;

    uses com.quorum.tessera.enclave.PayloadEncoder;

    uses com.quorum.tessera.enclave.EnclaveHolder;

    uses com.quorum.tessera.enclave.EnclaveClientFactory;

    uses com.quorum.tessera.enclave.Enclave;

    opens com.quorum.tessera.enclave to org.eclipse.persistence.moxy;

    provides com.quorum.tessera.enclave.PayloadEncoder with
        com.quorum.tessera.enclave.PayloadEncoderImpl;

    provides com.quorum.tessera.enclave.Enclave with
        com.quorum.tessera.enclave.EnclaveProvider;


}
