package com.github.nexus.key;

import com.github.nexus.config.ArgonOptions;
import com.github.nexus.config.Config;
import com.github.nexus.config.KeyData;
import com.github.nexus.config.KeyDataConfig;
import com.github.nexus.config.PrivateKeyType;
import com.github.nexus.keyenc.KeyConfig;
import com.github.nexus.keyenc.KeyEncryptor;

import com.github.nexus.nacl.Key;
import com.github.nexus.nacl.KeyPair;
import org.junit.Before;
import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.Arrays;
import java.util.Collections;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class KeyManagerTest {

    private static final String PRIVATE_KEY = "privateKey";

    private static final String PUBLIC_KEY = "publicKey";

    private KeyPair keyPair;

    private KeyEncryptor keyEncryptor;

    private KeyManager keyManager;

    @Before
    public void init() {

        this.keyPair = new KeyPair(
                new Key(PUBLIC_KEY.getBytes(UTF_8)),
                new Key(PRIVATE_KEY.getBytes(UTF_8))
        );

        KeyDataConfig privateKeyConfig = mock(KeyDataConfig.class);
        when(privateKeyConfig.getValue()).thenReturn(keyPair.getPrivateKey().toString());
        when(privateKeyConfig.getType()).thenReturn(PrivateKeyType.UNLOCKED);

        final Config configuration = mock(Config.class);

        KeyData keyData = new KeyData(privateKeyConfig, null, keyPair.getPublicKey().toString());
        when(configuration.getKeys()).thenReturn(Arrays.asList(keyData));

        this.keyEncryptor = mock(KeyEncryptor.class);

        this.keyManager = new KeyManagerImpl(keyEncryptor, configuration);
    }

    @Test
    public void initialisedWithNoKeysThrowsError() {
        //throws error because there is no default key
        final Config configuration = mock(Config.class);
        when(configuration.getKeys()).thenReturn(emptyList());
        final Throwable throwable = catchThrowable(
                () -> new KeyManagerImpl(keyEncryptor, configuration)
        );

        assertThat(throwable).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void publicKeyFoundGivenPrivateKey() {

        final Key publicKey = keyManager.getPublicKeyForPrivateKey(keyPair.getPrivateKey());

        assertThat(publicKey).isEqualTo(keyPair.getPublicKey());
    }

    @Test
    public void exceptionThrownWhenPrivateKeyNotFound() {

        final Key unknownKey = new Key("unknownKey".getBytes(UTF_8));
        final Throwable throwable = catchThrowable(() -> keyManager.getPublicKeyForPrivateKey(unknownKey));

        assertThat(throwable).isInstanceOf(RuntimeException.class).hasMessage("Private key dW5rbm93bktleQ== not found when searching for public key");

    }

    @Test
    public void privateKeyFoundGivenPublicKey() {

        final Key privateKey = keyManager.getPrivateKeyForPublicKey(keyPair.getPublicKey());

        assertThat(privateKey).isEqualTo(keyPair.getPrivateKey());
    }

    @Test
    public void exceptionThrownWhenPublicKeyNotFound() {

        final Key unknownKey = new Key("unknownKey".getBytes(UTF_8));
        final Throwable throwable = catchThrowable(() -> keyManager.getPrivateKeyForPublicKey(unknownKey));

        assertThat(throwable).isInstanceOf(RuntimeException.class).hasMessage("Public key dW5rbm93bktleQ== not found when searching for private key");

    }

    @Test
    public void loadedKeysCanBeSearchedFor() {

        KeyDataConfig privateKeyConfig = mock(KeyDataConfig.class);
        when(privateKeyConfig.getValue()).thenReturn(keyPair.getPrivateKey().toString());
        when(privateKeyConfig.getType()).thenReturn(PrivateKeyType.UNLOCKED);

        final KeyData keyData = new KeyData(privateKeyConfig, keyPair.getPrivateKey().toString(), keyPair.getPublicKey().toString());

        final KeyPair loaded = keyManager.loadKeypair(keyData);

        final Key result = keyManager.getPublicKeyForPrivateKey(loaded.getPrivateKey());

        assertThat(result).isEqualTo(keyPair.getPublicKey());
    }

    @Test
    public void getPublicKeysReturnsAllKeys() {
        final Set<Key> publicKeys = keyManager.getPublicKeys();

        assertThat(publicKeys).isNotEmpty();
        assertThat(publicKeys.size()).isEqualTo(1);
        assertThat(publicKeys.iterator().next().getKeyBytes()).isEqualTo(PUBLIC_KEY.getBytes());
    }

    @Test
    public void loadingPrivateKeyWithPasswordCallsKeyEncryptor() {

        KeyDataConfig privateKeyConfig = mock(KeyDataConfig.class);
        when(privateKeyConfig.getType()).thenReturn(PrivateKeyType.LOCKED);
        ArgonOptions argonOptions = mock(ArgonOptions.class);
        when(privateKeyConfig.getArgonOptions()).thenReturn(argonOptions);

        final KeyData keyData = new KeyData(privateKeyConfig, null, keyPair.getPublicKey().toString());

        doReturn(new Key(new byte[]{})).when(keyEncryptor).decryptPrivateKey(any(KeyConfig.class));

        keyManager.loadKeypair(keyData);

        verify(keyEncryptor)
                .decryptPrivateKey(any(KeyConfig.class));
    }

    @Test
    public void loadingPrivateKeyWithPasswordValueDoesnotCallEncrpter() {

        KeyDataConfig privateKeyConfig = mock(KeyDataConfig.class);

        final KeyData keyData = new KeyData(privateKeyConfig, keyPair.getPrivateKey().toString(), keyPair.getPublicKey().toString());

        doReturn(new Key(new byte[]{})).when(keyEncryptor).decryptPrivateKey(any(KeyConfig.class));

        keyManager.loadKeypair(keyData);

        verifyZeroInteractions(keyEncryptor);

    }

    @Test(expected = IllegalStateException.class)
    public void loadingPrivateKeyWithNopPasswordOrConfigThrowsException() {

        final KeyData keyData = new KeyData(null, null, keyPair.getPublicKey().toString());

        doReturn(new Key(new byte[]{})).when(keyEncryptor).decryptPrivateKey(any(KeyConfig.class));

        keyManager.loadKeypair(keyData);

    }

    @Test
    public void defaultKeyIsPopulated() {

        //the key manager is already set up with a keypair, so just check that
        assertThat(keyManager.defaultPublicKey()).isEqualTo(keyPair.getPublicKey());
    }

}
