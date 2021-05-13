package com.quorum.tessera.key.generation;

import static com.quorum.tessera.config.PrivateKeyType.UNLOCKED;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.PrivateKeyData;
import com.quorum.tessera.config.PrivateKeyType;
import com.quorum.tessera.config.keypairs.FilesystemKeyPair;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.encryption.PrivateKey;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.passwords.PasswordReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FileKeyGeneratorTest {

  private static final String PRIVATE_KEY = "privateKey";

  private static final String PUBLIC_KEY = "publicKey";

  private KeyPair keyPair;

  private Encryptor encryptor;

  private KeyEncryptor keyEncryptor;

  private PasswordReader passwordReader;

  private FileKeyGenerator generator;

  @Before
  public void init() {

    this.keyPair =
        new KeyPair(
            PublicKey.from(PUBLIC_KEY.getBytes(UTF_8)),
            PrivateKey.from(PRIVATE_KEY.getBytes(UTF_8)));

    this.encryptor = mock(Encryptor.class);
    this.keyEncryptor = mock(KeyEncryptor.class);
    this.passwordReader = mock(PasswordReader.class);

    when(passwordReader.requestUserPassword()).thenReturn(new char[0]);

    this.generator = new FileKeyGenerator(encryptor, keyEncryptor, passwordReader);
  }

  @After
  public void onTearDown() {
    verifyNoMoreInteractions(encryptor, keyEncryptor);
  }

  @Test
  public void generateFromKeyDataUnlockedPrivateKey() throws IOException {

    doReturn(keyPair).when(encryptor).generateNewKeys();

    String filename = UUID.randomUUID().toString();
    final Path tmpDir = Files.createTempDirectory("keygen").toAbsolutePath().resolve(filename);

    final FilesystemKeyPair generated = generator.generate(tmpDir.toString(), null, null);

    assertThat(generated).isInstanceOf(FilesystemKeyPair.class);
    assertThat(generated.getPublicKey()).isEqualTo("cHVibGljS2V5");
    assertThat(generated.getPrivateKey()).isEqualTo("cHJpdmF0ZUtleQ==");
    assertThat(generated.getInlineKeypair().getPrivateKeyConfig().getType()).isEqualTo(UNLOCKED);

    verify(encryptor).generateNewKeys();
  }

  @Test
  public void generateFromKeyDataLockedPrivateKey() throws IOException {

    when(passwordReader.requestUserPassword()).thenReturn("PASSWORD".toCharArray());

    final Path tempFolder = Files.createTempDirectory(UUID.randomUUID().toString());
    final String keyFilesName = tempFolder.resolve(UUID.randomUUID().toString()).toString();

    doReturn(keyPair).when(encryptor).generateNewKeys();

    final ArgonOptions argonOptions = new ArgonOptions("id", 1, 1, 1);

    final PrivateKeyData encryptedPrivateKey =
        new PrivateKeyData(null, null, null, null, argonOptions);

    doReturn(encryptedPrivateKey)
        .when(keyEncryptor)
        .encryptPrivateKey(any(PrivateKey.class), any(), eq(null));

    final PrivateKeyData encryptedKey =
        new PrivateKeyData(null, "snonce", "salt", "sbox", argonOptions);

    doReturn(encryptedKey)
        .when(keyEncryptor)
        .encryptPrivateKey(any(PrivateKey.class), any(), eq(null));

    final FilesystemKeyPair generated = generator.generate(keyFilesName, null, null);

    final KeyDataConfig pkd = generated.getInlineKeypair().getPrivateKeyConfig();
    assertThat(generated.getPublicKey()).isEqualTo("cHVibGljS2V5");
    assertThat(pkd.getSbox()).isEqualTo("sbox");
    assertThat(pkd.getSnonce()).isEqualTo("snonce");
    assertThat(pkd.getAsalt()).isEqualTo("salt");
    assertThat(pkd.getType()).isEqualTo(PrivateKeyType.LOCKED);

    verify(keyEncryptor).encryptPrivateKey(any(PrivateKey.class), any(), eq(null));
    verify(encryptor).generateNewKeys();
  }

  @Test
  public void providingPathSavesToFile() throws IOException {
    final Path tempFolder = Files.createTempDirectory(UUID.randomUUID().toString());
    final String keyFilesName = tempFolder.resolve("providingPathSavesToFile").toString();

    doReturn(keyPair).when(encryptor).generateNewKeys();

    final FilesystemKeyPair generated = generator.generate(keyFilesName, null, null);

    assertThat(Files.exists(tempFolder.resolve("providingPathSavesToFile.pub"))).isTrue();
    assertThat(Files.exists(tempFolder.resolve("providingPathSavesToFile.key"))).isTrue();

    verify(encryptor).generateNewKeys();
  }

  @Test
  public void providingNoPathSavesToFileInSameDirectory() throws IOException {
    Files.deleteIfExists(Paths.get(".pub"));
    Files.deleteIfExists(Paths.get(".key"));

    doReturn(keyPair).when(encryptor).generateNewKeys();

    final FilesystemKeyPair generated = generator.generate("", null, null);

    assertThat(Files.exists(Paths.get(".pub"))).isTrue();
    assertThat(Files.exists(Paths.get(".key"))).isTrue();

    verify(encryptor).generateNewKeys();

    Files.deleteIfExists(Paths.get(".pub"));
    Files.deleteIfExists(Paths.get(".key"));
  }

  @Test
  public void providingPathThatExistsThrowsError() throws IOException {
    final Path tempFolder = Files.createTempDirectory(UUID.randomUUID().toString());
    final String keyFilesName = tempFolder.resolve("key").toString();
    tempFolder.toFile().setWritable(false);

    doReturn(keyPair).when(encryptor).generateNewKeys();

    doReturn(new PrivateKeyData("", "", "", "", new ArgonOptions("", 1, 1, 1)))
        .when(keyEncryptor)
        .encryptPrivateKey(any(PrivateKey.class), any(), eq(null));

    final Throwable throwable = catchThrowable(() -> generator.generate(keyFilesName, null, null));

    assertThat(throwable).isInstanceOf(UncheckedIOException.class);

    assertThat(Files.exists(tempFolder.resolve("key.pub"))).isFalse();
    assertThat(Files.exists(tempFolder.resolve("key.key"))).isFalse();

    verify(encryptor).generateNewKeys();
  }
}
