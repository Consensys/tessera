package com.quorum.tessera.config.util;

import java.util.Optional;

// Provide a mockable wrapper for environment variable retrieval
public class EnvironmentVariableProvider {

  public String getEnv(String name) {
    return System.getenv(name);
  }

  public char[] getEnvAsCharArray(String name) {
    return Optional.ofNullable(System.getenv(name)).map(String::toCharArray).orElse(null);
  }

  public boolean hasEnv(String name) {
    return Optional.ofNullable(getEnv(name)).isPresent();
  }
}
