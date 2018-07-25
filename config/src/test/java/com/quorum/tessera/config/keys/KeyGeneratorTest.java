package com.quorum.tessera.config.keys;

import com.quorum.tessera.argon2.ArgonOptions;
import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.PrivateKeyData;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.nacl.KeyPair;
import com.quorum.tessera.nacl.NaclFacade;
import com.quorum.tessera.config.PrivateKeyType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

        final KeyData generated = generator.generate(new KeyDataConfig(null, PrivateKeyType.UNLOCKED));

        assertThat(generated.getPublicKey()).isEqualTo("cHVibGljS2V5");
        assertThat(generated.getPrivateKey()).isEqualTo("cHJpdmF0ZUtleQ==");
        assertThat(generated.getConfig().getType()).isEqualTo(PrivateKeyType.UNLOCKED);

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

        final KeyDataConfig privateKeyConfig = new KeyDataConfig(
            new PrivateKeyData(
                null,
                null,
                null,
                null,
                new com.quorum.tessera.config.ArgonOptions(
                    argonOptions.getAlgorithm(),
                    argonOptions.getIterations(),
                    argonOptions.getMemory(),
                    argonOptions.getParallelism()
                ),
                "PASSWORD"
            ),
            PrivateKeyType.LOCKED
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

        final KeyData generated = generator.generate(privateKeyConfig);

        assertThat(generated.getPublicKey()).isEqualTo("cHVibGljS2V5");
        assertThat(generated.getConfig().getPassword()).isEqualTo("PASSWORD");
        assertThat(generated.getConfig().getSbox()).isEqualTo("sbox");
        assertThat(generated.getConfig().getSnonce()).isEqualTo("snonce");
        assertThat(generated.getConfig().getAsalt()).isEqualTo("salt");
        assertThat(generated.getConfig().getType()).isEqualTo(PrivateKeyType.LOCKED);

        verify(keyEncryptor).encryptPrivateKey(any(Key.class), anyString());
        verify(nacl).generateNewKeys();
    }

}
