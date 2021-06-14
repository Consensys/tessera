package com.quorum.tessera.encryption.ec;

import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.encryption.EncryptorFactory;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EllipticalCurveEncryptorFactory implements EncryptorFactory {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(EllipticalCurveEncryptorFactory.class);

  @Override
  public String getType() {
    return "EC";
  }

  @Override
  public Encryptor create(Map<String, String> properties) {
    LOGGER.debug("Creating an EC implementation of EncryptorFactory");

    final Map<String, String> props =
        Optional.ofNullable(properties).orElse(Collections.emptyMap());
    String symmetricCipher = props.getOrDefault("symmetricCipher", "AES/GCM/NoPadding");
    String ellipticCurve = props.getOrDefault("ellipticCurve", "secp256r1");
    int nonceLength = Integer.parseInt(props.getOrDefault("nonceLength", "24"));
    int sharedKeyLength = Integer.parseInt(props.getOrDefault("sharedKeyLength", "32"));

    return new EllipticalCurveEncryptor(
        symmetricCipher, ellipticCurve, nonceLength, sharedKeyLength);
  }
}
