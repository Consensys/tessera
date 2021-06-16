package com.quorum.tessera.config.internal;

import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;

public class ConfigFactoryProvider {

  public static ConfigFactory provider() {
    KeyEncryptorFactory keyEncryptorFactory = KeyEncryptorFactory.newFactory();
    return new JaxbConfigFactory(keyEncryptorFactory);
  }
}
