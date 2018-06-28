package com.github.nexus.keygen;

import com.github.nexus.configuration.Configuration;
import com.github.nexus.keyenc.KeyEncryptor;
import com.github.nexus.nacl.Key;
import com.github.nexus.nacl.KeyPair;
import com.github.nexus.nacl.NaclFacade;
import org.junit.Before;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

public class KeyGeneratorTest {

    private static final String privateKey = "privateKey";

    private static final String publicKey = "publicKey";

    private KeyPair keyPair;

    private Path keygenPath;

    private NaclFacade nacl;

    private KeyEncryptor keyEncryptor;

    private KeyGenerator generator;

    @Before
    public void init() throws IOException {

        this.keyPair = new KeyPair(
            new Key(publicKey.getBytes(UTF_8)),
            new Key(privateKey.getBytes(UTF_8))
        );

        this.keygenPath = Files.createTempDirectory(UUID.randomUUID().toString());

        this.nacl = mock(NaclFacade.class);
        this.keyEncryptor = mock(KeyEncryptor.class);

        final Configuration configuration = mock(Configuration.class);
        doReturn(keygenPath).when(configuration).keygenBasePath();

        this.generator = new KeyGeneratorImpl(nacl, configuration, keyEncryptor);

    }

    @Test
    public void generatingNewKeysCreatesTwoKeys() {

        final String keyName = "testkey";

        doReturn(keyPair).when(nacl).generateNewKeys();

        final KeyGenerator.Pair<String, String> generated = generator.generateNewKeys(keyName);

        final JsonReader reader = Json.createReader(new StringReader(generated.right));
        final String privateKeyBase64 = reader.readObject().getJsonObject("data").getString("bytes");

        final byte[] publicKey = Base64.getDecoder().decode(generated.left);
        final byte[] privateKey = Base64.getDecoder().decode(privateKeyBase64);

        assertThat(new Key(publicKey)).isEqualTo(keyPair.getPublicKey());
        assertThat(new Key(privateKey)).isEqualTo(keyPair.getPrivateKey());

        verify(nacl).generateNewKeys();
    }

    @Test
    public void generatingNewKeysWithPasswordCreatesCorrectJson() {

        final String keyName = "testkey";
        final String password = "testpassword";

        doReturn(keyPair).when(nacl).generateNewKeys();
        doReturn(Json.createObjectBuilder().add("test", "obj").build())
            .when(keyEncryptor)
            .encryptPrivateKey(keyPair.getPrivateKey(), password);

        final KeyGenerator.Pair<String, String> generated = generator.generateNewKeys(keyName, password);

        final JsonReader reader = Json.createReader(new StringReader(generated.right));

        final JsonObject expected = Json.createObjectBuilder()
            .add("type", "argon2sbox")
            .add("data", Json.createObjectBuilder().add("test", "obj"))
            .build();

        assertThat(reader.readObject()).isEqualTo(expected);

    }

    @Test
    public void writingToFileHappensIfRequested() {

        final String keyName = "testkey";
        final InputStream inputStream = new ByteArrayInputStream("\n\ny".getBytes());

        doReturn(keyPair).when(nacl).generateNewKeys();
        doReturn(Json.createObjectBuilder().build())
            .when(keyEncryptor)
            .encryptPrivateKey(any(Key.class), eq(keyName));

        generator.promptForGeneration(keyName, inputStream);

        final boolean pubExists = Files.exists(keygenPath.resolve(keyName + ".pub"));
        final boolean privExists = Files.exists(keygenPath.resolve(keyName + ".key"));

        assertThat(pubExists).isTrue();
        assertThat(privExists).isTrue();

    }

    @Test
    public void writingToFileDoesntHappenIfNotRequested() {

        final String keyName = "testkey";
        final InputStream inputStream = new ByteArrayInputStream("\n\nn".getBytes());

        doReturn(keyPair).when(nacl).generateNewKeys();

        generator.promptForGeneration(keyName, inputStream);

        final boolean pubExists = Files.exists(keygenPath.resolve(keyName + ".pub"));
        final boolean privExists = Files.exists(keygenPath.resolve(keyName + ".key"));

        assertThat(pubExists).isFalse();
        assertThat(privExists).isFalse();

    }

    @Test
    public void providingPasswordToPromptEncryptsKey() {

        final String keyName = "testkey";
        final InputStream inputStream = new ByteArrayInputStream("pass\nn".getBytes());

        doReturn(keyPair).when(nacl).generateNewKeys();
        doReturn(Json.createObjectBuilder().add("test", "obj").build())
            .when(keyEncryptor)
            .encryptPrivateKey(keyPair.getPrivateKey(), "pass");

        generator.promptForGeneration(keyName, inputStream);

        verify(nacl).generateNewKeys();
        verify(keyEncryptor).encryptPrivateKey(keyPair.getPrivateKey(), "pass");

    }

    @Test
    public void generatingNewKeysThrowsExceptionIfCantWrite() throws IOException {

        final String keyName = "testkey";
        final InputStream inputStream = new ByteArrayInputStream("\n\ny".getBytes());

        Files.write(keygenPath.resolve(keyName + ".pub"), "tst".getBytes());
        Files.write(keygenPath.resolve(keyName + ".key"), "tst".getBytes());

        doReturn(keyPair).when(nacl).generateNewKeys();

        final Throwable throwable = catchThrowable(() -> generator.promptForGeneration(keyName, inputStream));

        assertThat(throwable).isNotNull().isInstanceOf(RuntimeException.class);
        assertThat(throwable.getCause()).isInstanceOf(IOException.class);

    }

}
