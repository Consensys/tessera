package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import java.util.ServiceLoader;

public interface KeyDataMarshaller {

  KeyData marshal(ConfigKeyPair keyPair);

  static KeyDataMarshaller create() {
    return ServiceLoader.load(KeyDataMarshaller.class).findFirst().get();
  }
}
