package com.quorum.tessera.config;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public interface KeyVaultConfig {

  KeyVaultType getKeyVaultType();

  default Map<String, String> getProperties() {
    return Collections.EMPTY_MAP;
  }

  default boolean hasProperty(String... name) {
    return Stream.of(name).allMatch(n -> getProperties().containsKey(n));
  }

  default Optional<String> getProperty(String name) {
    return Optional.ofNullable(getProperties().get(name));
  }

  static KeyVaultConfig create() {
    return new DefaultKeyVaultConfig();
  }
}
