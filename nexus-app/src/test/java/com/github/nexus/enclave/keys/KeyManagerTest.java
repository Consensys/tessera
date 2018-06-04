package com.github.nexus.enclave.keys;

import com.github.nexus.enclave.keys.model.Key;
import com.github.nexus.enclave.keys.model.KeyPair;
import com.github.nexus.encryption.NaclFacade;
import org.junit.Before;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class KeyManagerTest {

    private static final String privateKey = "privateKey";

    private static final String publicKey = "publicKey";

    private Path keygenPath;

    private KeyPair keyPair;

    private NaclFacade naclFacade;

    private KeyManager keyManager;

    @Before
    public void init() throws IOException {

        this.keyPair = new KeyPair(
                new Key(publicKey.getBytes(UTF_8)),
                new Key(privateKey.getBytes(UTF_8))
        );

        this.keygenPath = Files.createTempDirectory(UUID.randomUUID().toString());

        this.naclFacade = mock(NaclFacade.class);

        this.keyManager = new KeyManagerImpl(keygenPath.toString(), naclFacade, Collections.singleton(keyPair));

    }

    @Test
    public void public_key_found_given_private_key() {

        final Key publicKey = keyManager.getPublicKeyForPrivateKey(keyPair.getPrivateKey());

        assertThat(publicKey).isEqualTo(keyPair.getPublicKey());
    }

    @Test
    public void exception_thrown_when_private_key_not_found() {

        final Key unknownKey = new Key("unknownKey".getBytes(UTF_8));
        final Throwable throwable = catchThrowable(() -> keyManager.getPublicKeyForPrivateKey(unknownKey));

        assertThat(throwable).isInstanceOf(RuntimeException.class).hasMessage("Public key not found!");

    }

    @Test
    public void private_key_found_given_public_key() {

        final Key privateKey = keyManager.getPrivateKeyForPublicKey(keyPair.getPublicKey());

        assertThat(privateKey).isEqualTo(keyPair.getPrivateKey());
    }

    @Test
    public void exception_thrown_when_public_key_not_found() {

        final Key unknownKey = new Key("unknownKey".getBytes(UTF_8));
        final Throwable throwable = catchThrowable(() -> keyManager.getPrivateKeyForPublicKey(unknownKey));

        assertThat(throwable).isInstanceOf(RuntimeException.class).hasMessage("Private key not found!");

    }

    @Test
    public void generating_new_keys_creates_two_files() throws IOException {

        final String keyName = "testkey";

        doReturn(keyPair).when(naclFacade).generateNewKeys();

        final KeyPair generated = keyManager.generateNewKeys(keyName);

        assertThat(generated).isEqualTo(keyPair);

        final byte[] publicKey = Files.readAllBytes(keygenPath.resolve("testkey.pub"));
        final byte[] privateKeyJson = Files.readAllBytes(keygenPath.resolve("testkey.key"));

        final JsonReader reader = Json.createReader(new StringReader(new String(privateKeyJson, UTF_8)));
        final String privateKey = reader.readObject().getJsonObject("data").getString("bytes");


        assertThat(new Key(publicKey)).isEqualTo(keyPair.getPublicKey());
        assertThat(new Key(privateKey.getBytes(UTF_8))).isEqualTo(keyPair.getPrivateKey());
    }

    @Test
    public void generating_new_keys_throws_exception_if_cant_write() throws IOException {

        final String keyName = "testkey";

        Files.write(keygenPath.resolve(keyName + ".pub"), "tst".getBytes());
        Files.write(keygenPath.resolve(keyName + ".key"), "tst".getBytes());

        doReturn(keyPair).when(naclFacade).generateNewKeys();

        final Throwable throwable = catchThrowable(() -> keyManager.generateNewKeys(keyName));

        assertThat(throwable).isNotNull().isInstanceOf(RuntimeException.class);
        assertThat(throwable.getCause()).isInstanceOf(IOException.class);
    }

    @Test
    public void load_nonexistant_keys_throws_exception() {

        final Path publicKeyPath = keygenPath.resolve("unknownKey.pub");
        final Path privateKeyPath = keygenPath.resolve("unknownKey.key");

        final Throwable throwable = catchThrowable(() -> keyManager.loadKeypair(publicKeyPath, privateKeyPath));

        assertThat(throwable).isNotNull().isInstanceOf(RuntimeException.class);
        assertThat(throwable.getCause()).isInstanceOf(IOException.class);

    }

    @Test
    public void load_keys_returns_keypair() {
        final String keyName = "testkey";
        doReturn(keyPair).when(naclFacade).generateNewKeys();
        final KeyPair generated = keyManager.generateNewKeys(keyName);

        final Path publicKeyPath = keygenPath.resolve("testkey.pub");
        final Path privateKeyPath = keygenPath.resolve("testkey.key");

        final KeyPair loaded = keyManager.loadKeypair(publicKeyPath, privateKeyPath);

        assertThat(generated).isEqualTo(loaded);
    }

    @Test
    public void loaded_keys_can_be_searched_for() {
        final String keyName = "testkey";
        doReturn(keyPair).when(naclFacade).generateNewKeys();
        final KeyPair generated = keyManager.generateNewKeys(keyName);

        final Path publicKeyPath = keygenPath.resolve("testkey.pub");
        final Path privateKeyPath = keygenPath.resolve("testkey.key");

        final KeyPair loaded = keyManager.loadKeypair(publicKeyPath, privateKeyPath);

        final Key publicKey = keyManager.getPublicKeyForPrivateKey(loaded.getPrivateKey());

        assertThat(publicKey).isEqualTo(keyPair.getPublicKey());
    }

    @Test
    public void keymanager_loads_given_keys_when_instantiated_with_paths() {
        final String keyName = "testkey";
        doReturn(keyPair).when(naclFacade).generateNewKeys();
        final KeyPair generated = keyManager.generateNewKeys(keyName);

        final Path publicKeyPath = keygenPath.resolve("testkey.pub");
        final Path privateKeyPath = keygenPath.resolve("testkey.key");

        final KeyManager pathLoadingKeyManager
                = new KeyManagerImpl(keygenPath.toString(), naclFacade, singletonList(publicKeyPath), singletonList(privateKeyPath));

        //check the keys got loaded okay
        final Key publicKey = keyManager.getPublicKeyForPrivateKey(keyPair.getPrivateKey());

        assertThat(publicKey).isEqualTo(keyPair.getPublicKey());

    }

    @Test
    public void different_number_of_keys_throws_exception() {

        final Throwable throwable = catchThrowable(
                () -> new KeyManagerImpl(keygenPath.toString(), naclFacade, emptyList(), singletonList(Paths.get(".")))
        );

        assertThat(throwable).isInstanceOf(RuntimeException.class).hasMessage("Key sizes don't match");
    }

}
