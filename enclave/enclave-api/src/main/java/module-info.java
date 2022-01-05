module tessera.enclave.api {
  requires tessera.config;
  requires tessera.encryption.api;
  requires tessera.keyvault.api;
  requires tessera.shared;
  requires org.bouncycastle.provider;
  requires org.slf4j;
  requires com.fasterxml.jackson.databind;
  requires com.fasterxml.jackson.dataformat.cbor;

  exports com.quorum.tessera.enclave;

  uses com.quorum.tessera.enclave.PayloadEncoder;
  uses com.quorum.tessera.enclave.EnclaveHolder;
  uses com.quorum.tessera.enclave.EnclaveClientFactory;
  uses com.quorum.tessera.enclave.EnclaveClient;
  uses com.quorum.tessera.enclave.Enclave;
  uses com.quorum.tessera.enclave.EnclaveServer;
  uses com.quorum.tessera.enclave.PayloadDigest;

  opens com.quorum.tessera.enclave to
      org.eclipse.persistence.moxy;

  exports com.quorum.tessera.enclave.internal;

  opens com.quorum.tessera.enclave.internal to
      org.eclipse.persistence.moxy;

  provides com.quorum.tessera.enclave.PayloadEncoder with
      com.quorum.tessera.enclave.PayloadEncoderImpl,
      com.quorum.tessera.enclave.CBOREncoder;
  provides com.quorum.tessera.enclave.Enclave with
      com.quorum.tessera.enclave.EnclaveProvider;
  provides com.quorum.tessera.enclave.EnclaveServer with
      com.quorum.tessera.enclave.EnclaveServerProvider;
  provides com.quorum.tessera.enclave.PayloadDigest with
      com.quorum.tessera.enclave.DefaultPayloadDigest,
      com.quorum.tessera.enclave.SHA512256PayloadDigest;
}
