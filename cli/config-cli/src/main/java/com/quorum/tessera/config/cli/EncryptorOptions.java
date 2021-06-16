package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.EncryptorConfig;
import com.quorum.tessera.config.EncryptorType;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import picocli.CommandLine;

class EncryptorOptions {

  @CommandLine.Option(
      names = {"--encryptor.type"},
      description = "Valid values: ${COMPLETION-CANDIDATES}")
  private EncryptorType type;

  @CommandLine.Option(names = {"--encryptor.symmetricCipher"})
  private String symmetricCipher;

  @CommandLine.Option(names = {"--encryptor.ellipticCurve"})
  private String ellipticCurve;

  @CommandLine.Option(names = {"--encryptor.nonceLength"})
  private String nonceLength;

  @CommandLine.Option(names = {"--encryptor.sharedKeyLength"})
  private String sharedKeyLength;

  EncryptorConfig parseEncryptorConfig() {
    final EncryptorConfig encryptorConfig = new EncryptorConfig();

    // we set the default here instead of in the option annotation as enum values cannot be used in
    // annotations
    if (Objects.isNull(type)) {
      type = EncryptorType.NACL;
    }

    Map<String, String> properties = new HashMap<>();
    if (type == EncryptorType.EC) {

      Optional.ofNullable(symmetricCipher).ifPresent(v -> properties.put("symmetricCipher", v));
      Optional.ofNullable(ellipticCurve).ifPresent(v -> properties.put("ellipticCurve", v));
      Optional.ofNullable(nonceLength).ifPresent(v -> properties.put("nonceLength", v));
      Optional.ofNullable(sharedKeyLength).ifPresent(v -> properties.put("sharedKeyLength", v));
    }

    encryptorConfig.setType(type);
    encryptorConfig.setProperties(properties);

    return encryptorConfig;
  }
}
