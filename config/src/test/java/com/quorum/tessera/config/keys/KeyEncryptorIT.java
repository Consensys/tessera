package com.quorum.tessera.config.keys;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.EncryptorConfig;
import com.quorum.tessera.config.EncryptorType;
import com.quorum.tessera.config.PrivateKeyData;
import com.quorum.tessera.encryption.PrivateKey;
import java.util.Base64;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyEncryptorIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(KeyEncryptorIT.class);

  private static final String pkeyBase64 = "kwMSmHpIhCFFoOCyGkHkJyfLVxsa9Mj3Y23sFYRLwLM=";

  private static final char[] password = "PASS_WORD".toCharArray();

  private KeyEncryptor keyEncryptor;

  private PrivateKey privateKey;

  @Before
  public void init() {
    this.privateKey = PrivateKey.from(Base64.getDecoder().decode(pkeyBase64));

    this.keyEncryptor =
        KeyEncryptorFactory.newFactory()
            .create(
                new EncryptorConfig() {
                  {
                    setType(EncryptorType.NACL);
                  }
                });
  }

  @Test
  public void encryptAndDecryptOnKeyIsSuccessful() {
    ArgonOptions argonOptions = new ArgonOptions("i", 10, 1048576, 4);

    final PrivateKeyData privateKeyData =
        keyEncryptor.encryptPrivateKey(privateKey, password, argonOptions);
    final PrivateKey decryptedKey = keyEncryptor.decryptPrivateKey(privateKeyData, password);

    assertThat(decryptedKey).isEqualTo(privateKey);
  }
}
