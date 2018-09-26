package com.quorum.tessera.config.keys;

import com.quorum.tessera.argon2.Argon2;
import com.quorum.tessera.argon2.ArgonResult;
import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.PrivateKeyData;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.nacl.NaclFacade;
import com.quorum.tessera.nacl.Nonce;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class KeyEncryptorTest {

    private Argon2 argon2;

    private NaclFacade nacl;

    private KeyEncryptor keyEncryptor;

    @Before
    public void init() {

        this.argon2 = mock(Argon2.class);
        this.nacl = mock(NaclFacade.class);

        this.keyEncryptor = new KeyEncryptorImpl(argon2, nacl);

    }

    @Test
    public void encryptingKeyReturnsCorrectJson() {

        final Key key = new Key(new byte[]{1, 2, 3, 4, 5});
        final String password = "pass";
        final ArgonResult result = new ArgonResult(
            new com.quorum.tessera.argon2.ArgonOptions("i", 1, 1, 1),
            new byte[]{},
            new byte[]{}
        );

        doReturn(result).when(argon2).hash(eq(password), any(byte[].class));
        doReturn(new Nonce(new byte[]{})).when(nacl).randomNonce();
        doReturn(new byte[]{}).when(nacl).sealAfterPrecomputation(any(byte[].class), any(Nonce.class), any(Key.class));

        final PrivateKeyData privateKey = this.keyEncryptor.encryptPrivateKey(key, password, null);

        final ArgonOptions aopts = privateKey.getArgonOptions();

        assertThat(privateKey.getSbox()).isNotNull();
        assertThat(privateKey.getAsalt()).isNotNull();
        assertThat(privateKey.getSnonce()).isNotNull();

        assertThat(aopts).isNotNull();
        assertThat(aopts.getMemory()).isNotNull();
        assertThat(aopts.getParallelism()).isNotNull();
        assertThat(aopts.getIterations()).isNotNull();
        assertThat(aopts.getAlgorithm()).isNotNull();

        verify(argon2).hash(eq(password), any(byte[].class));
        verify(nacl).randomNonce();
        verify(nacl).sealAfterPrecomputation(any(byte[].class), any(Nonce.class), any(Key.class));

    }

    @Test
    public void providingArgonOptionsEncryptsKey() {

        final Key key = new Key(new byte[]{1, 2, 3, 4, 5});
        final String password = "pass";
        final ArgonResult result = new ArgonResult(
            new com.quorum.tessera.argon2.ArgonOptions("i", 5, 6, 7),
            new byte[]{},
            new byte[]{}
        );

        doReturn(result).when(argon2).hash(any(com.quorum.tessera.argon2.ArgonOptions.class), eq(password), any(byte[].class));
        doReturn(new Nonce(new byte[]{})).when(nacl).randomNonce();
        doReturn(new byte[]{}).when(nacl).sealAfterPrecomputation(any(byte[].class), any(Nonce.class), any(Key.class));

        final PrivateKeyData privateKey
            = this.keyEncryptor.encryptPrivateKey(key, password, new ArgonOptions("i", 5, 6, 7));

        final ArgonOptions aopts = privateKey.getArgonOptions();

        assertThat(privateKey.getSbox()).isNotNull();
        assertThat(privateKey.getAsalt()).isNotNull();
        assertThat(privateKey.getSnonce()).isNotNull();

        assertThat(aopts).isNotNull();
        assertThat(aopts.getIterations()).isNotNull().isEqualTo(5);
        assertThat(aopts.getMemory()).isNotNull().isEqualTo(6);
        assertThat(aopts.getParallelism()).isNotNull().isEqualTo(7);
        assertThat(aopts.getAlgorithm()).isNotNull();

        verify(argon2).hash(any(com.quorum.tessera.argon2.ArgonOptions.class), eq(password), any(byte[].class));
        verify(nacl).randomNonce();
        verify(nacl).sealAfterPrecomputation(any(byte[].class), any(Nonce.class), any(Key.class));

    }

    @Test
    public void nullKeyGivesError() {

        final Throwable throwable = catchThrowable(() -> keyEncryptor.encryptPrivateKey(null, "", null));

        assertThat(throwable).isInstanceOf(NullPointerException.class);

    }

    @Test
    public void correntJsonGivesDecryptedKey() {

        final String password = "pass";

        final ArgonOptions argonOptions = new ArgonOptions("i", 1, 1, 1);

        final PrivateKeyData lockedPrivateKey
            = new PrivateKeyData("", "", "uZAfjmMwEepP8kzZCnmH6g==", "", argonOptions, password);

        doReturn(new byte[]{1, 2, 3})
            .when(this.nacl)
            .openAfterPrecomputation(any(byte[].class), any(Nonce.class), any(Key.class));

        doReturn(new ArgonResult(null, new byte[]{}, new byte[]{4, 5, 6}))
            .when(this.argon2)
            .hash(any(com.quorum.tessera.argon2.ArgonOptions.class), eq(password), any(byte[].class));

        final Key key = this.keyEncryptor.decryptPrivateKey(lockedPrivateKey, password);

        assertThat(key.getKeyBytes()).isEqualTo(new byte[]{1, 2, 3});

        verify(this.nacl).openAfterPrecomputation(any(byte[].class), any(Nonce.class), any(Key.class));
        verify(this.argon2).hash(any(com.quorum.tessera.argon2.ArgonOptions.class), eq(password), any(byte[].class));

    }

}
