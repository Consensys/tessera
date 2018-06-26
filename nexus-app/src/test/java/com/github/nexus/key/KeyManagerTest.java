//package com.github.nexus.key;
//
//import com.github.nexus.TestConfiguration;
//import com.github.nexus.configuration.Configuration;
//import com.github.nexus.configuration.model.KeyData;
//import com.github.nexus.keygen.KeyEncryptor;
//import com.github.nexus.nacl.Key;
//import com.github.nexus.nacl.KeyPair;
//import org.junit.Before;
//import org.junit.Test;
//
//import javax.json.Json;
//import javax.json.JsonObject;
//import java.util.List;
//import java.util.NoSuchElementException;
//import java.util.Set;
//
//import static java.nio.charset.StandardCharsets.UTF_8;
//import static java.util.Collections.singletonList;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.catchThrowable;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//
//public class KeyManagerTest {
//
//    private static final String privateKey = "privateKey";
//
//    private static final String publicKey = "publicKey";
//
//    private KeyPair keyPair;
//
//    private KeyEncryptor keyEncryptor;
//
//    private KeyManager keyManager;
//
//    @Before
//    public void init() {
//
//        this.keyPair = new KeyPair(
//            new Key(publicKey.getBytes(UTF_8)),
//            new Key(privateKey.getBytes(UTF_8))
//        );
//
//        final JsonObject privateKeyJson = Json.createObjectBuilder()
//            .add("type", "unlocked")
//            .add("data", Json.createObjectBuilder().add("bytes", keyPair.getPrivateKey().toString()))
//            .build();
//
//        final Configuration configuration = new TestConfiguration(){
//
//            @Override
//            public List<KeyData> keyData() {
//                return singletonList(
//                    new KeyData(keyPair.getPublicKey().toString(), privateKeyJson, null)
//                );
//            }
//
//        };
//
//        this.keyEncryptor = mock(KeyEncryptor.class);
//
//        this.keyManager = new KeyManagerImpl(keyEncryptor, configuration);
//    }
//
//    @Test
//    public void initialisedWithNoKeysThrowsError() {
//        //throws error because there is no default key
//
//        final Throwable throwable = catchThrowable(
//            () -> new KeyManagerImpl(keyEncryptor, new TestConfiguration())
//        );
//
//        assertThat(throwable).isInstanceOf(NoSuchElementException.class);
//    }
//
//    @Test
//    public void publicKeyFoundGivenPrivateKey() {
//
//        final Key publicKey = keyManager.getPublicKeyForPrivateKey(keyPair.getPrivateKey());
//
//        assertThat(publicKey).isEqualTo(keyPair.getPublicKey());
//    }
//
//    @Test
//    public void exceptionThrownWhenPrivateKeyNotFound() {
//
//        final Key unknownKey = new Key("unknownKey".getBytes(UTF_8));
//        final Throwable throwable = catchThrowable(() -> keyManager.getPublicKeyForPrivateKey(unknownKey));
//
//        assertThat(throwable).isInstanceOf(RuntimeException.class).hasMessage("Private key dW5rbm93bktleQ== not found when searching for public key");
//
//    }
//
//    @Test
//    public void privateKeyFoundGivenPublicKey() {
//
//        final Key privateKey = keyManager.getPrivateKeyForPublicKey(keyPair.getPublicKey());
//
//        assertThat(privateKey).isEqualTo(keyPair.getPrivateKey());
//    }
//
//    @Test
//    public void exceptionThrownWhenPublicKeyNotFound() {
//
//        final Key unknownKey = new Key("unknownKey".getBytes(UTF_8));
//        final Throwable throwable = catchThrowable(() -> keyManager.getPrivateKeyForPublicKey(unknownKey));
//
//        assertThat(throwable).isInstanceOf(RuntimeException.class).hasMessage("Public key dW5rbm93bktleQ== not found when searching for private key");
//
//    }
//
//    @Test
//    public void loadKeysReturnsKeypair() {
//        final JsonObject privateKeyJson = Json.createObjectBuilder()
//            .add("type", "unlocked")
//            .add("data", Json.createObjectBuilder().add("bytes", keyPair.getPrivateKey().toString()))
//            .build();
//
//        final KeyData keyData = new KeyData(keyPair.getPublicKey().toString(), privateKeyJson, null);
//
//        final KeyPair loaded = keyManager.loadKeypair(keyData);
//
//        assertThat(keyPair).isEqualTo(loaded);
//    }
//
//    @Test
//    public void loadedKeysCanBeSearchedFor() {
//        final JsonObject privateKey = Json.createObjectBuilder()
//            .add("type", "unlocked")
//            .add("data", Json.createObjectBuilder().add("bytes", keyPair.getPrivateKey().toString()))
//            .build();
//
//        final KeyData keyData = new KeyData(keyPair.getPublicKey().toString(), privateKey, null);
//
//        final KeyPair loaded = keyManager.loadKeypair(keyData);
//
//        final Key publicKey = keyManager.getPublicKeyForPrivateKey(loaded.getPrivateKey());
//
//        assertThat(publicKey).isEqualTo(keyPair.getPublicKey());
//    }
//
//    @Test
//    public void getPublicKeysReturnsAllKeys(){
//        final Set<Key> publicKeys = keyManager.getPublicKeys();
//
//        assertThat(publicKeys).isNotEmpty();
//        assertThat(publicKeys.size()).isEqualTo(1);
//        assertThat(publicKeys.iterator().next().getKeyBytes()).isEqualTo(publicKey.getBytes());
//    }
//
//    @Test
//    public void loadingPrivateKeyWithPasswordCallsKeyEncryptor() {
//        final JsonObject privateKey = Json.createObjectBuilder()
//            .add("type", "argon2sbox")
//            .add("data", Json.createObjectBuilder().build())
//            .build();
//
//        final KeyData keyData = new KeyData(keyPair.getPublicKey().toString(), privateKey, "pass");
//
//        doReturn(new Key(new byte[]{})).when(keyEncryptor).decryptPrivateKey(any(JsonObject.class), eq("pass"));
//
//        keyManager.loadKeypair(keyData);
//
//        verify(keyEncryptor).decryptPrivateKey(any(JsonObject.class), eq("pass"));
//    }
//
//    @Test
//    public void defaultKeyIsPopulated() {
//
//        //the key manager is already set up with a keypair, so just check that
//        assertThat(keyManager.defaultPublicKey()).isEqualTo(keyPair.getPublicKey());
//    }
//
//}
