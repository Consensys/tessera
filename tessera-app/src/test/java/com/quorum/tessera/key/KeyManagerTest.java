package com.quorum.tessera.key;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.nacl.Key;
import org.junit.Before;
import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KeyManagerTest {

    private static final Key PRIVATE_KEY = new Key("privateKey".getBytes());

    private static final Key PUBLIC_KEY = new Key("publicKey".getBytes());

    private KeyManager keyManager;

    @Before
    public void init() {

        final Config configuration = mock(Config.class);

        final KeyData keyData = new KeyData(null, PRIVATE_KEY.toString(), PUBLIC_KEY.toString(), null, null);

        final KeyConfiguration keyConfig = new KeyConfiguration(null, null, singletonList(keyData));

        when(configuration.getKeys()).thenReturn(keyConfig);

        this.keyManager = new KeyManagerImpl(configuration);
    }

    @Test
    public void initialisedWithNoKeysThrowsError() {
        //throws error because there is no default key
        final Config configuration = mock(Config.class);
        final KeyConfiguration keyConfig = new KeyConfiguration(null, null, emptyList());
        when(configuration.getKeys()).thenReturn(keyConfig);

        final Throwable throwable = catchThrowable(() -> new KeyManagerImpl(configuration));

        assertThat(throwable).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void publicKeyFoundGivenPrivateKey() {
        final Key publicKey = this.keyManager.getPublicKeyForPrivateKey(PRIVATE_KEY);

        assertThat(publicKey).isEqualTo(PUBLIC_KEY);
    }

    @Test
    public void exceptionThrownWhenPrivateKeyNotFound() {
        final Key unknownKey = new Key("unknownKey".getBytes());
        final Throwable throwable = catchThrowable(() -> this.keyManager.getPublicKeyForPrivateKey(unknownKey));

        assertThat(throwable)
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Private key dW5rbm93bktleQ== not found when searching for public key");

    }

    @Test
    public void privateKeyFoundGivenPublicKey() {
        final Key privateKey = this.keyManager.getPrivateKeyForPublicKey(PUBLIC_KEY);

        assertThat(privateKey).isEqualTo(PRIVATE_KEY);
    }

    @Test
    public void exceptionThrownWhenPublicKeyNotFound() {
        final Key unknownKey = new Key("unknownKey".getBytes());
        final Throwable throwable = catchThrowable(() -> this.keyManager.getPrivateKeyForPublicKey(unknownKey));

        assertThat(throwable)
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Public key dW5rbm93bktleQ== not found when searching for private key");
    }

    @Test
    public void getPublicKeysReturnsAllKeys() {
        final Set<Key> publicKeys = this.keyManager.getPublicKeys();

        assertThat(publicKeys.size()).isEqualTo(1);
        assertThat(publicKeys.iterator().next()).isEqualTo(PUBLIC_KEY);
    }

    @Test
    public void defaultKeyIsPopulated() {
        //the key manager is already set up with a keypair, so just check that
        assertThat(this.keyManager.defaultPublicKey()).isEqualTo(PUBLIC_KEY);
    }

}
