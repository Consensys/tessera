package com.quorum.tessera.config.keys;

import com.quorum.tessera.config.*;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.nacl.KeyPair;
import com.quorum.tessera.nacl.NaclFacade;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

public class KeyGeneratorTest {

    private static final String PRIVATE_KEY = "privateKey";

    private static final String PUBLIC_KEY = "publicKey";

    private KeyPair keyPair;

    private NaclFacade nacl;

    private KeyEncryptor keyEncryptor;

    private InputStream inputStream;

    private KeyGenerator generator;

    @Before
    public void init() {

        this.keyPair = new KeyPair(
            new Key(PUBLIC_KEY.getBytes(UTF_8)),
            new Key(PRIVATE_KEY.getBytes(UTF_8))
        );

        this.nacl = mock(NaclFacade.class);
        this.keyEncryptor = mock(KeyEncryptor.class);
        this.inputStream = new ByteArrayInputStream(System.lineSeparator().getBytes());

        this.generator = new KeyGeneratorImpl(nacl, keyEncryptor, inputStream);

    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(nacl, keyEncryptor);
    }

    @Test
    public void generateFromKeyDataUnlockedPrivateKey() {

        doReturn(keyPair).when(nacl).generateNewKeys();

        final KeyData generated = generator.generate(UUID.randomUUID().toString());

        assertThat(generated.getPublicKey()).isEqualTo("cHVibGljS2V5");
        assertThat(generated.getPrivateKey()).isEqualTo("cHJpdmF0ZUtleQ==");
        assertThat(generated.getConfig().getType()).isEqualTo(PrivateKeyType.UNLOCKED);

        verify(nacl).generateNewKeys();

    }

    @Test
    public void generateFromKeyDataLockedPrivateKey() throws IOException {

        this.generator = new KeyGeneratorImpl(
            nacl,
            keyEncryptor,
            new ByteArrayInputStream(("PASSWORD" + System.lineSeparator()).getBytes())
        );

        final Path tempFolder = Files.createTempDirectory(UUID.randomUUID().toString());
        final String keyFilesName = tempFolder.resolve(UUID.randomUUID().toString()).toString();

        doReturn(keyPair).when(nacl).generateNewKeys();

        final ArgonOptions argonOptions = new ArgonOptions("id", 1, 1, 1);

        final PrivateKeyData encryptedPrivateKey = new PrivateKeyData(null, null, null, null, argonOptions, null);

        doReturn(encryptedPrivateKey).when(keyEncryptor).encryptPrivateKey(any(Key.class), anyString());

        final PrivateKeyData encryptedKey = new PrivateKeyData(null, "snonce", "salt", "sbox", argonOptions, "PASSWORD");

        doReturn(encryptedKey).when(keyEncryptor).encryptPrivateKey(any(Key.class), anyString());

        final KeyData generated = generator.generate(keyFilesName);

        assertThat(generated.getPublicKey()).isEqualTo("cHVibGljS2V5");
        assertThat(generated.getConfig().getPassword()).isEqualTo("PASSWORD");
        assertThat(generated.getConfig().getSbox()).isEqualTo("sbox");
        assertThat(generated.getConfig().getSnonce()).isEqualTo("snonce");
        assertThat(generated.getConfig().getAsalt()).isEqualTo("salt");
        assertThat(generated.getConfig().getType()).isEqualTo(PrivateKeyType.LOCKED);

        verify(keyEncryptor).encryptPrivateKey(any(Key.class), anyString());
        verify(nacl).generateNewKeys();
    }

    @Test
    public void providingPathSavesToFile() throws IOException {
        final Path tempFolder = Files.createTempDirectory(UUID.randomUUID().toString());
        final String keyFilesName = tempFolder.resolve("providingPathSavesToFile").toString();

        this.inputStream = new ByteArrayInputStream(System.lineSeparator().getBytes());

        this.generator = new KeyGeneratorImpl(nacl, keyEncryptor, inputStream);

        doReturn(keyPair).when(nacl).generateNewKeys();

        final KeyData generated = generator.generate(keyFilesName);

        assertThat(Files.exists(tempFolder.resolve("providingPathSavesToFile.pub"))).isTrue();
        assertThat(Files.exists(tempFolder.resolve("providingPathSavesToFile.key"))).isTrue();

        verify(nacl).generateNewKeys();
    }

    @Test
    public void providingNoPathSavesToFileInSameDirectory() throws IOException {
        Files.deleteIfExists(Paths.get(".pub"));
        Files.deleteIfExists(Paths.get(".key"));

        this.inputStream = new ByteArrayInputStream(System.lineSeparator().getBytes());

        this.generator = new KeyGeneratorImpl(nacl, keyEncryptor, inputStream);

        doReturn(keyPair).when(nacl).generateNewKeys();

        final KeyData generated = generator.generate("");

        assertThat(Files.exists(Paths.get(".pub"))).isTrue();
        assertThat(Files.exists(Paths.get(".key"))).isTrue();

        verify(nacl).generateNewKeys();

        Files.deleteIfExists(Paths.get(".pub"));
        Files.deleteIfExists(Paths.get(".key"));
    }

    @Test
    public void providingPathThatExistsThrowsError() throws IOException {
        final Path tempFolder = Files.createTempDirectory(UUID.randomUUID().toString());
        final String keyFilesName = tempFolder.resolve("key").toString();
        tempFolder.toFile().setWritable(false);

        doReturn(keyPair).when(nacl).generateNewKeys();

        doReturn(new PrivateKeyData("", "", "", "", new ArgonOptions("", 1, 1, 1), ""))
            .when(keyEncryptor)
            .encryptPrivateKey(any(Key.class), anyString());

        this.inputStream = new ByteArrayInputStream(keyFilesName.getBytes());
        this.generator = new KeyGeneratorImpl(nacl, keyEncryptor, inputStream);

        final Throwable throwable = catchThrowable(() -> generator.generate(keyFilesName));

        assertThat(throwable).isInstanceOf(UncheckedIOException.class);

        assertThat(Files.exists(tempFolder.resolve("key.pub"))).isFalse();
        assertThat(Files.exists(tempFolder.resolve("key.key"))).isFalse();

        verify(nacl).generateNewKeys();
        verify(keyEncryptor).encryptPrivateKey(any(Key.class), anyString());
    }

}
