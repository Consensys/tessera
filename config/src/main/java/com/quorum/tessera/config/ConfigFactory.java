package com.quorum.tessera.config;

import com.quorum.tessera.config.internal.ConfigHolder;
import java.io.InputStream;
import java.util.ServiceLoader;

public interface ConfigFactory {

  Config create(InputStream configData);

  static ConfigFactory create() {
    return ServiceLoader.load(ConfigFactory.class).findFirst().get();
  }

  default Config getConfig() {
    return ConfigHolder.INSTANCE.getConfig();
  }

  void store(Config config);
}
