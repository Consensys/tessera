package com.quorum.tessera.enclave;

import com.quorum.tessera.config.Config;
import java.util.ServiceLoader;

/**
 * Creates clients which connect to remote instances of an enclave.
 *
 * @param <T> the type of remote enclave to create a client for
 */
public interface EnclaveClientFactory<T extends EnclaveClient> {

  T create(Config config);

  static EnclaveClientFactory create() {
    // TODO: return the stream and let the caller deal with it
    return ServiceLoader.load(EnclaveClientFactory.class).findFirst().get();
  }
}
