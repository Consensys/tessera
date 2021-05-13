package com.quorum.tessera.encryption;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * * A factory for providing the implementation of the {@link Encryptor} with all its dependencies
 * set up
 */
public interface EncryptorFactory {

  /**
   * Retrieves a preconfigured Encryptor
   *
   * @return the implementation of the {@link Encryptor}
   */
  default Encryptor create() {
    return create(Collections.emptyMap());
  }

  Encryptor create(Map<String, String> properties);

  String getType();

  /**
   * Retrieves the implementation of the factory from the service loader
   *
   * @return the factory implementation that will provide instances of that implementations {@link
   *     Encryptor}
   */
  static EncryptorFactory newFactory(String type) {

    return ServiceLoader.load(EncryptorFactory.class).stream()
        .map(ServiceLoader.Provider::get)
        .filter(f -> f.getType().equals(type))
        .findAny()
        .orElseThrow(
            () -> {
              String message =
                  ServiceLoader.load(EncryptorFactory.class).stream()
                      .map(Objects::toString)
                      .collect(Collectors.joining(","));
              return new EncryptorFactoryNotFoundException(type + " Found only " + message);
            });
  }
}
