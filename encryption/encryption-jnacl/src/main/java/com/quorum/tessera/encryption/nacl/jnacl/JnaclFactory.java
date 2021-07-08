package com.quorum.tessera.encryption.nacl.jnacl;

import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.encryption.EncryptorFactory;
import java.security.SecureRandom;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Provides the JNaCL implementation of the {@link Encryptor} */
public class JnaclFactory implements EncryptorFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(JnaclFactory.class);

  @Override
  public Encryptor create(Map<String, String> properties) {
    LOGGER.debug("Creating a JNaCl implementation of EncryptorFactory");

    final SecureRandom secureRandom = new SecureRandom();
    final JnaclSecretBox secretBox = new JnaclSecretBox();

    return new Jnacl(secureRandom, secretBox);
  }

  @Override
  public String getType() {
    return "NACL";
  }
}
