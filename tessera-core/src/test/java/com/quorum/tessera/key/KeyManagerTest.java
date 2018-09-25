package com.quorum.tessera.key;

import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keypairs.DirectKeyPair;
import com.quorum.tessera.nacl.Key;
import org.junit.Before;
import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class KeyManagerTest {

    private static final Key PRIVATE_KEY = new Key("privateKey".getBytes());

    private static final Key PUBLIC_KEY = new Key("publicKey".getBytes());

    private static final Key FORWARDING_KEY = new Key("forwardingKey".getBytes());

    private KeyManager keyManager;

    @Before
    public void init() {

        final ConfigKeyPair configKeyPair = new DirectKeyPair(PUBLIC_KEY.toString(), PRIVATE_KEY.toString());

        this.keyManager = new KeyManagerImpl(singleton(configKeyPair), singleton(FORWARDING_KEY));
    }

    @Test
    public void initialisedWithNoKeysThrowsError() {
        //throws error because there is no default key
        final Throwable throwable = catchThrowable(() -> new KeyManagerImpl(emptyList(), emptyList()));

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

    @Test
    public void forwardingKeysContainsOnlyOneKey() {
        assertThat(this.keyManager.getForwardingKeys()).hasSize(1).containsExactlyInAnyOrder(FORWARDING_KEY);
    }

}
