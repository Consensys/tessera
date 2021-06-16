package com.quorum.tessera.config.internal;

import com.quorum.tessera.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
// Need to sort this out
public enum ConfigHolder {
  INSTANCE;

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigHolder.class);

  private Config config;

  void setConfig(Config config) {
    LOGGER.debug("set confing {}", config);
    this.config = config;
  }

  public Config getConfig() {
    return config;
  }
}
