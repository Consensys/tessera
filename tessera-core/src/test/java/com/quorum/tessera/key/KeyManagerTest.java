package com.quorum.tessera.key;

import com.quorum.tessera.encryption.KeyUtil;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.encryption.PrivateKey;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keypairs.DirectKeyPair;
import com.quorum.tessera.key.exception.KeyNotFoundException;
import org.junit.Before;
import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class KeyManagerTest {

    private static final PublicKey PUBLIC_KEY = PublicKey.from("publicKey".getBytes());
    
     private static final PrivateKey PRIVATE_KEY = PrivateKey.from("privateKey".getBytes());
    
     private static final PublicKey FORWARDING_KEY = PublicKey.from("forwardingKey".getBytes());
     
    private KeyManager keyManager;

    @Before
    public void init() {
        
        String encodedPublicKey = KeyUtil.encodeToBase64(PUBLIC_KEY);
        String encodedPrivateKey = KeyUtil.encodeToBase64(PRIVATE_KEY);
        
        final ConfigKeyPair configKeyPair = new DirectKeyPair(encodedPublicKey, encodedPrivateKey);

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
        final PublicKey publicKey = this.keyManager.getPublicKeyForPrivateKey(PRIVATE_KEY);

        assertThat(publicKey).isEqualTo(PUBLIC_KEY);
    }

    @Test
    public void exceptionThrownWhenPrivateKeyNotFound() {
        final PrivateKey unknownKey = PrivateKey.from("unknownKey".getBytes());
        final Throwable throwable = catchThrowable(() -> this.keyManager.getPublicKeyForPrivateKey(unknownKey));

        assertThat(throwable)
            .isInstanceOf(KeyNotFoundException.class)
            .hasMessage("Private key dW5rbm93bktleQ== not found when searching for public key");

    }

    @Test
    public void privateKeyFoundGivenPublicKey() {
        final PrivateKey privateKey = this.keyManager.getPrivateKeyForPublicKey(PUBLIC_KEY);

        assertThat(privateKey).isEqualTo(PRIVATE_KEY);
    }

    @Test
    public void exceptionThrownWhenPublicKeyNotFound() {
        final PublicKey unknownKey = PublicKey.from("unknownKey".getBytes());
        final Throwable throwable = catchThrowable(() -> this.keyManager.getPrivateKeyForPublicKey(unknownKey));

        assertThat(throwable)
            .isInstanceOf(KeyNotFoundException.class)
            .hasMessage("Public key dW5rbm93bktleQ== not found when searching for private key");
    }

    @Test
    public void getPublicKeysReturnsAllKeys() {
        final Set<PublicKey> publicKeys = this.keyManager.getPublicKeys();

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
        assertThat(this.keyManager.getForwardingKeys()).hasSize(1)
                .containsExactlyInAnyOrder(FORWARDING_KEY);
    }

}
