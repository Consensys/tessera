package com.github.nexus.keygen;

import com.github.nexus.argon2.Argon2;
import com.github.nexus.nacl.Key;
import com.github.nexus.nacl.NaclFacade;
import com.github.nexus.nacl.NaclFacadeFactory;
import org.junit.Before;
import org.junit.Test;

import javax.json.JsonObject;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyEncryptorIT {

    private static final String pkeyBase64 = "kwMSmHpIhCFFoOCyGkHkJyfLVxsa9Mj3Y23sFYRLwLM=";

    private static final String password = "PASS_WORD";

    private KeyEncryptor keyEncryptor;

    private Key privateKey;

    @Before
    public void init() {

        final NaclFacade nacl = NaclFacadeFactory.newFactory().create();
        final Argon2 argon2 = Argon2.create();

        this.privateKey = new Key(Base64.getDecoder().decode(pkeyBase64));

        this.keyEncryptor = new KeyEncryptorImpl(argon2, nacl);
    }

    @Test
    public void encryptAndDecryptOnKeyIsSuccessful() {

        final JsonObject jsonObject = keyEncryptor.encryptPrivateKey(privateKey, password);

        final Key decryptedKey = keyEncryptor.decryptPrivateKey(jsonObject, password);

        assertThat(decryptedKey).isEqualTo(privateKey);

    }

}
