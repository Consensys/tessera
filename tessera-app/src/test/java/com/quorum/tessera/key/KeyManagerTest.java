package com.quorum.tessera.key;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.nacl.KeyPair;
import org.junit.Before;
import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KeyManagerTest {

    private static final String PRIVATE_KEY = "privateKey";

    private static final String PUBLIC_KEY = "publicKey";

    private KeyPair keyPair;

    private KeyManager keyManager;

    @Before
    public void init() {

        this.keyPair = new KeyPair(
            new Key(PUBLIC_KEY.getBytes(UTF_8)),
            new Key(PRIVATE_KEY.getBytes(UTF_8))
        );

        final Config configuration = mock(Config.class);

        KeyData keyData = new KeyData(
            null,
            keyPair.getPrivateKey().toString(),
            keyPair.getPublicKey().toString()
        );

        when(configuration.getKeys()).thenReturn(singletonList(keyData));

        this.keyManager = new KeyManagerImpl(configuration);
    }

    @Test
    public void initialisedWithNoKeysThrowsError() {
        //throws error because there is no default key
        final Config configuration = mock(Config.class);
        when(configuration.getKeys()).thenReturn(emptyList());
        final Throwable throwable = catchThrowable(() -> new KeyManagerImpl(configuration));

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
    public void getPublicKeysReturnsAllKeys() {
        final Set<Key> publicKeys = keyManager.getPublicKeys();

        assertThat(publicKeys).isNotEmpty();
        assertThat(publicKeys.size()).isEqualTo(1);
        assertThat(publicKeys.iterator().next().getKeyBytes()).isEqualTo(PUBLIC_KEY.getBytes());
    }

    @Test
    public void defaultKeyIsPopulated() {
        //the key manager is already set up with a keypair, so just check that
        assertThat(keyManager.defaultPublicKey()).isEqualTo(keyPair.getPublicKey());
    }

}
