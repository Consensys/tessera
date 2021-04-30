
module tessera.enclave.enclave.api.main {
    requires tessera.config.main;
    requires tessera.encryption.encryption.api.main;
    requires tessera.key.vault.key.vault.api.main;
    requires tessera.shared.main;
    requires org.bouncycastle.provider;
    requires org.slf4j;
    requires tuweni.bytes;
    requires tuweni.rlp;

    exports com.quorum.tessera.enclave;

    uses com.quorum.tessera.enclave.PayloadEncoder;

    uses com.quorum.tessera.enclave.EnclaveHolder;

    uses com.quorum.tessera.enclave.EnclaveClientFactory;

    uses com.quorum.tessera.enclave.EnclaveClient;

    uses com.quorum.tessera.enclave.Enclave;

    uses com.quorum.tessera.enclave.EnclaveServer;

    uses com.quorum.tessera.enclave.PayloadDigest;

    opens com.quorum.tessera.enclave to org.eclipse.persistence.moxy;

    provides com.quorum.tessera.enclave.PayloadEncoder with
        com.quorum.tessera.enclave.PayloadEncoderImpl;

    provides com.quorum.tessera.enclave.Enclave with
        com.quorum.tessera.enclave.EnclaveProvider;

    provides com.quorum.tessera.enclave.EnclaveServer with
        com.quorum.tessera.enclave.EnclaveServerProvider;

    provides com.quorum.tessera.enclave.PayloadDigest with
        com.quorum.tessera.enclave.DefaultPayloadDigest,com.quorum.tessera.enclave.SHA512256PayloadDigest;
}
