package com.github.nexus.keys;

import com.github.nexus.TestConfiguration;
import com.github.nexus.configuration.Configuration;
import com.github.nexus.nacl.Key;
import com.github.nexus.nacl.KeyPair;
import org.junit.Before;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.List;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class KeyManagerTest {

    private static final String privateKey = "privateKey";

    private static final String publicKey = "publicKey";

    private KeyPair keyPair;

    private KeyManager keyManager;

    @Before
    public void init() {

        this.keyPair = new KeyPair(
            new Key(publicKey.getBytes(UTF_8)),
            new Key(privateKey.getBytes(UTF_8))
        );

        final String privateKeyJson = Json.createObjectBuilder()
            .add("type", "unlocked")
            .add("data", Json.createObjectBuilder().add("bytes", keyPair.getPrivateKey().toString()))
            .build()
            .toString();

        final Configuration configuration = new TestConfiguration(){

            @Override
            public List<String> publicKeys() {
                return singletonList(keyPair.getPublicKey().toString());
            }

            @Override
            public String privateKeys() {
                return privateKeyJson;
            }

        };

        this.keyManager = new KeyManagerImpl(configuration);
    }

    @Test
    public void initialisedWithNoKeys() {

        this.keyManager = new KeyManagerImpl(new TestConfiguration());

        assertThat(keyManager).extracting("ourKeys").containsExactly(emptySet());
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
    public void loadKeysReturnsKeypair() {
        final JsonObject privateKeyJson = Json.createObjectBuilder()
            .add("type", "unlocked")
            .add("data", Json.createObjectBuilder().add("bytes", keyPair.getPrivateKey().toString()))
            .build();

        final KeyPair loaded = keyManager.loadKeypair(keyPair.getPublicKey().toString(), privateKeyJson);

        assertThat(keyPair).isEqualTo(loaded);
    }

    @Test
    public void loadedKeysCanBeSearchedFor() {
        final JsonObject privateKey = Json.createObjectBuilder()
            .add("type", "unlocked")
            .add("data", Json.createObjectBuilder().add("bytes", keyPair.getPrivateKey().toString()))
            .build();

        final KeyPair loaded = keyManager.loadKeypair(keyPair.getPublicKey().toString(), privateKey);

        final Key publicKey = keyManager.getPublicKeyForPrivateKey(loaded.getPrivateKey());

        assertThat(publicKey).isEqualTo(keyPair.getPublicKey());
    }

    @Test
    public void differentNumberOfKeysThrowsException() {
        final JsonObject privateKey = Json.createObjectBuilder()
            .add("type", "unlocked")
            .add("data", Json.createObjectBuilder().add("bytes", keyPair.getPrivateKey().toString()))
            .build();

        final Throwable throwable = catchThrowable(() -> new KeyManagerImpl(emptyList(), singletonList(privateKey)));

        assertThat(throwable).isInstanceOf(RuntimeException.class).hasMessage("Initial key list sizes don't match");
    }

    @Test
    public void testGetPublicKeys(){
        Set<Key> publicKeys = keyManager.getPublicKeys();

        assertThat(publicKeys).isNotEmpty();
        assertThat(publicKeys.size()).isEqualTo(1);
        assertThat(publicKeys.iterator().next().getKeyBytes()).isEqualTo(publicKey.getBytes());
    }

}
