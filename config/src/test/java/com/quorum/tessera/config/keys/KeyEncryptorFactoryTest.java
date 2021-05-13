package com.quorum.tessera.config.keys;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.EncryptorConfig;
import com.quorum.tessera.config.EncryptorType;
import org.junit.Test;

public class KeyEncryptorFactoryTest {

  @Test
  public void newFactory() {

    final KeyEncryptorFactory keyEncryptorFactory = KeyEncryptorFactory.newFactory();

    assertThat(keyEncryptorFactory).isNotNull();
  }

  @Test
  public void create() {
    final KeyEncryptorFactory keyEncryptorFactory = new KeyEncryptorFactoryImpl();

    EncryptorConfig encryptorConfig = new EncryptorConfig();
    encryptorConfig.setType(EncryptorType.NACL);

    KeyEncryptor result = keyEncryptorFactory.create(encryptorConfig);

    assertThat(result).isNotNull().isExactlyInstanceOf(KeyEncryptorImpl.class);
  }
}
