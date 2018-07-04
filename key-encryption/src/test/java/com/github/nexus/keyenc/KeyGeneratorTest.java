package com.github.nexus.keyenc;

import com.github.nexus.argon2.ArgonOptions;
import com.github.nexus.config.KeyData;
import com.github.nexus.config.PrivateKey;
import com.github.nexus.config.PrivateKeyData;
import com.github.nexus.nacl.Key;
import com.github.nexus.nacl.KeyPair;
import com.github.nexus.nacl.NaclFacade;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.github.nexus.config.PrivateKeyType.LOCKED;
import static com.github.nexus.config.PrivateKeyType.UNLOCKED;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class KeyGeneratorTest {

    private static final String PRIVATE_KEY = "privateKey";

    private static final String PUBLIC_KEY = "publicKey";

    private KeyPair keyPair;

    private NaclFacade nacl;

    private KeyEncryptor keyEncryptor;

    private KeyGenerator generator;

    @Before
    public void init() {

        this.keyPair = new KeyPair(
            new Key(PUBLIC_KEY.getBytes(UTF_8)),
            new Key(PRIVATE_KEY.getBytes(UTF_8))
        );

        this.nacl = mock(NaclFacade.class);
        this.keyEncryptor = mock(KeyEncryptor.class);

        this.generator = new KeyGeneratorImpl(nacl, keyEncryptor);

    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(nacl, keyEncryptor);
    }

    @Test
    public void generateFromKeyDataUnlockedPrivateKey() {

        doReturn(keyPair).when(nacl).generateNewKeys();

        final PrivateKey privateKey = new PrivateKey(null, UNLOCKED);
        final KeyData keyData = new KeyData(privateKey, "publicKey");

        final KeyData generated = generator.generate(keyData);

        assertThat(generated.getPublicKey()).isEqualTo("cHVibGljS2V5");
        assertThat(generated.getPrivateKey().getValue()).isEqualTo("cHJpdmF0ZUtleQ==");
        assertThat(generated.getPrivateKey().getType()).isEqualTo(UNLOCKED);

        verify(nacl).generateNewKeys();

    }

    @Test
    public void generateFromKeyDataLockedPrivateKey() {

        doReturn(keyPair).when(nacl).generateNewKeys();

        final ArgonOptions argonOptions = new ArgonOptions("id", 1, 1, 1);

        final KeyConfig encrypedPrivateKey = KeyConfig.Builder
            .create()
            .argonOptions(argonOptions)
            .build();

        doReturn(encrypedPrivateKey).when(keyEncryptor).encryptPrivateKey(any(Key.class), anyString());

        final PrivateKey privateKey = new PrivateKey(
            new PrivateKeyData(
                null,
                null,
                null,
                null,
                new com.github.nexus.config.ArgonOptions(
                    argonOptions.getAlgorithm(),
                    argonOptions.getIterations(),
                    argonOptions.getMemory(),
                    argonOptions.getParallelism()
                ),
                "PASSWORD"
            ),
            LOCKED
        );

        final KeyConfig encryptedKey = KeyConfig.Builder
            .create()
            .snonce("snonce".getBytes())
            .sbox("sbox".getBytes())
            .argonOptions(argonOptions)
            .asalt("salt".getBytes())
            .password("PASSWORD")
            .build();

        doReturn(encryptedKey).when(keyEncryptor).encryptPrivateKey(any(Key.class), anyString());

        final KeyData keyData = new KeyData(privateKey, null);

        final KeyData generated = generator.generate(keyData);

        assertThat(generated.getPublicKey()).isEqualTo("cHVibGljS2V5");
        assertThat(generated.getPrivateKey().getPassword()).isEqualTo("PASSWORD");
        assertThat(generated.getPrivateKey().getSbox()).isEqualTo("sbox");
        assertThat(generated.getPrivateKey().getSnonce()).isEqualTo("snonce");
        assertThat(generated.getPrivateKey().getAsalt()).isEqualTo("salt");
        assertThat(generated.getPrivateKey().getType()).isEqualTo(LOCKED);

        verify(keyEncryptor).encryptPrivateKey(any(Key.class), anyString());
        verify(nacl).generateNewKeys();
    }

}
