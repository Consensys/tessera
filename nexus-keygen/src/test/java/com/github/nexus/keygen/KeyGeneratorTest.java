package com.github.nexus.keygen;

import com.github.nexus.configuration.Configuration;
import com.github.nexus.nacl.Key;
import com.github.nexus.nacl.KeyPair;
import com.github.nexus.nacl.NaclFacade;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

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
    public void generatingNewKeysCreatesTwoFiles() throws IOException {

        final String keyName = "testkey";

        doReturn(keyPair).when(nacl).generateNewKeys();

        final KeyPair generated = generator.generateNewKeys(keyName);

        assertThat(generated).isEqualTo(keyPair);

        final String publicKeyBase64 = new String(Files.readAllBytes(keygenPath.resolve("testkey.pub")), UTF_8);
        final byte[] privateKeyJson = Files.readAllBytes(keygenPath.resolve("testkey.key"));

        final JsonReader reader = Json.createReader(new StringReader(new String(privateKeyJson, UTF_8)));
        final String privateKeyBase64 = reader.readObject().getJsonObject("data").getString("bytes");

        final byte[] publicKey = Base64.getDecoder().decode(publicKeyBase64);
        final byte[] privateKey = Base64.getDecoder().decode(privateKeyBase64);

        assertThat(new Key(publicKey)).isEqualTo(keyPair.getPublicKey());
        assertThat(new Key(privateKey)).isEqualTo(keyPair.getPrivateKey());
    }

    @Test
    public void generatingNewKeysWithPasswordCreatesCorrectJson() throws IOException {

        final String keyName = "testkey";
        final String password = "testpassword";

        doReturn(keyPair).when(nacl).generateNewKeys();
        Mockito.doReturn(Json.createObjectBuilder().add("test", "obj").build())
            .when(keyEncryptor)
            .encryptPrivateKey(keyPair.getPrivateKey(), password);

        generator.generateNewKeys(keyName, password);

        final byte[] privateKeyJson = Files.readAllBytes(keygenPath.resolve("testkey.key"));

        final JsonReader reader = Json.createReader(new StringReader(new String(privateKeyJson, UTF_8)));

        final JsonObject expected = Json.createObjectBuilder()
            .add("type", "argon2sbox")
            .add("data", Json.createObjectBuilder().add("test", "obj"))
            .build();

        Assertions.assertThat(reader.readObject()).isEqualTo(expected);

    }

    @Test
    public void generatingNewKeysThrowsExceptionIfCantWrite() throws IOException {

        final String keyName = "testkey";

        Files.write(keygenPath.resolve(keyName + ".pub"), "tst".getBytes());
        Files.write(keygenPath.resolve(keyName + ".key"), "tst".getBytes());

        doReturn(keyPair).when(nacl).generateNewKeys();

        final Throwable throwable = catchThrowable(() -> generator.generateNewKeys(keyName));

        assertThat(throwable).isNotNull().isInstanceOf(RuntimeException.class);
        assertThat(throwable.getCause()).isInstanceOf(IOException.class);
    }


}
