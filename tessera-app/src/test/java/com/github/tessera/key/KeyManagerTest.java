package com.github.tessera.key;

import com.github.tessera.config.KeyData;
import com.github.tessera.nacl.Key;
import com.github.tessera.nacl.KeyPair;
import org.junit.Before;
import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class KeyManagerTest {

    private static final Key PRIVATE_KEY = new Key("privateKey".getBytes(UTF_8));

    private static final Key PUBLIC_KEY = new Key("publicKey".getBytes(UTF_8));

    private static final Key FORWARDING_KEY = new Key("forwarding-key".getBytes());

    private static final KeyPair KEYPAIR = new KeyPair(PUBLIC_KEY, PRIVATE_KEY);

    private KeyManager keyManager;

    @Before
    public void init() {

        final KeyData keyData = new KeyData(
            null,
            KEYPAIR.getPrivateKey().toString(),
            KEYPAIR.getPublicKey().toString()
        );

        this.keyManager = new KeyManagerImpl(singletonList(keyData), singletonList(FORWARDING_KEY));
    }

    @Test
    public void initialisedWithNoKeysThrowsError() {
        //throws error because there is no default key
        final Throwable throwable = catchThrowable(() -> new KeyManagerImpl(emptyList(), emptyList()));

        assertThat(throwable).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void publicKeyFoundGivenPrivateKey() {

        final Key publicKey = keyManager.getPublicKeyForPrivateKey(KEYPAIR.getPrivateKey());

        assertThat(publicKey).isEqualTo(KEYPAIR.getPublicKey());
    }

    @Test
    public void exceptionThrownWhenPrivateKeyNotFound() {

        final Key unknownKey = new Key("unknownKey".getBytes(UTF_8));
        final Throwable throwable = catchThrowable(() -> keyManager.getPublicKeyForPrivateKey(unknownKey));

        assertThat(throwable)
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Private key dW5rbm93bktleQ== not found when searching for public key");

    }

    @Test
    public void privateKeyFoundGivenPublicKey() {

        final Key privateKey = keyManager.getPrivateKeyForPublicKey(KEYPAIR.getPublicKey());

        assertThat(privateKey).isEqualTo(KEYPAIR.getPrivateKey());
    }

    @Test
    public void exceptionThrownWhenPublicKeyNotFound() {

        final Key unknownKey = new Key("unknownKey".getBytes(UTF_8));
        final Throwable throwable = catchThrowable(() -> keyManager.getPrivateKeyForPublicKey(unknownKey));

        assertThat(throwable)
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Public key dW5rbm93bktleQ== not found when searching for private key");

    }

    @Test
    public void getPublicKeysReturnsAllKeys() {
        final Set<Key> publicKeys = keyManager.getPublicKeys();

        assertThat(publicKeys).hasSize(1).containsExactlyInAnyOrder(PUBLIC_KEY);
    }

    @Test
    public void defaultKeyIsPopulated() {
        //the key manager is already set up with a KEYPAIR, so just check that
        assertThat(keyManager.defaultPublicKey()).isEqualTo(KEYPAIR.getPublicKey());
    }

    @Test
    public void forwardingKeysReadFromConfigurationCorrectly() {

        final Set<Key> forwardingKeys = this.keyManager.getForwardingKeys();

        assertThat(forwardingKeys).hasSize(1).containsExactlyInAnyOrder(FORWARDING_KEY);

    }

}
