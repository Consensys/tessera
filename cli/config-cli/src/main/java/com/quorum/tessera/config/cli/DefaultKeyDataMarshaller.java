package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.util.KeyDataUtil;

public class DefaultKeyDataMarshaller implements KeyDataMarshaller {
  public KeyData marshal(ConfigKeyPair keyPair) {
    return KeyDataUtil.marshal(keyPair);
  }
}
