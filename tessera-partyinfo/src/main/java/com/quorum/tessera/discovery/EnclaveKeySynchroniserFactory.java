package com.quorum.tessera.discovery;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;

public class EnclaveKeySynchroniserFactory implements EnclaveKeySynchroniser {

  public static EnclaveKeySynchroniser provider() {
    Enclave enclave = EnclaveFactory.create().enclave().get();
    NetworkStore networkStore = NetworkStore.getInstance();
    return new EnclaveKeySynchroniserImpl(enclave, networkStore);
  }

  private final EnclaveKeySynchroniser synchroniser;

  public EnclaveKeySynchroniserFactory() {
    this(provider());
  }

  protected EnclaveKeySynchroniserFactory(EnclaveKeySynchroniser synchroniser) {
    this.synchroniser = synchroniser;
  }

  @Override
  public void syncKeys() {
    synchroniser.syncKeys();
  }
}
