package com.quorum.tessera.config.keypairs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.PrivateKeyData;
import com.quorum.tessera.config.PrivateKeyType;
import com.quorum.tessera.config.keys.KeyEncryptor;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;

public class FilesystemKeyPairTest {

  private KeyEncryptor keyEncryptor;

  @Before
  public void onSetup() {
    keyEncryptor = mock(KeyEncryptor.class);
  }

  @Test
  public void gettersWorkAsExpected() {
    Path pub = Paths.get("pubPath");
    Path priv = Paths.get("privPath");

    FilesystemKeyPair keyPair = new FilesystemKeyPair(pub, priv, keyEncryptor);

    assertThat(keyPair.getPublicKeyPath()).isEqualByComparingTo(pub);
    assertThat(keyPair.getPrivateKeyPath()).isEqualByComparingTo(priv);
  }

  @Test
  public void setPasswordIsRetrievable() throws IOException, URISyntaxException {
    final Path pubFile = Files.createTempFile(UUID.randomUUID().toString(), ".pub");
    final Path privFile = Paths.get(getClass().getResource("/unlockedprivatekey.json").toURI());

    final String pub = "public";
    Files.write(pubFile, pub.getBytes());

    final FilesystemKeyPair filesystemKeyPair =
        new FilesystemKeyPair(pubFile, privFile, keyEncryptor);
    filesystemKeyPair.withPassword("password".toCharArray());

    assertThat(String.valueOf(filesystemKeyPair.getPassword())).isEqualTo("password");
  }

  @Test
  public void getInlineKeypairReturnsKeysReadFromFile() throws Exception {

    Path pubFile = Files.createTempFile(UUID.randomUUID().toString(), ".pub");
    Path privFile = Paths.get(getClass().getResource("/unlockedprivatekey.json").toURI());

    String pub = "public";
    Files.write(pubFile, pub.getBytes());

    FilesystemKeyPair filesystemKeyPair = new FilesystemKeyPair(pubFile, privFile, keyEncryptor);

    KeyDataConfig privKeyDataConfig =
        new KeyDataConfig(
            new PrivateKeyData(
                "Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=", null, null, null, null),
            PrivateKeyType.UNLOCKED);

    InlineKeypair expected = new InlineKeypair(pub, privKeyDataConfig, keyEncryptor);

    assertThat(filesystemKeyPair.getInlineKeypair())
        .isEqualToComparingFieldByFieldRecursively(expected);
    assertThat(filesystemKeyPair.getPublicKey()).isEqualTo(pub);
    assertThat(filesystemKeyPair.getPrivateKey())
        .isEqualTo("Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=");
  }

  @Test
  public void setPasswordIsRetrievableOnNullInlineKey() throws IOException, URISyntaxException {
    final Path pubFile =
        Files.createTempFile(UUID.randomUUID().toString(), ".pub").resolveSibling("nonexistantkey");
    final Path privFile = Paths.get(getClass().getResource("/unlockedprivatekey.json").toURI());

    final FilesystemKeyPair filesystemKeyPair =
        new FilesystemKeyPair(pubFile, privFile, keyEncryptor);
    filesystemKeyPair.withPassword("password".toCharArray());

    assertThat(String.valueOf(filesystemKeyPair.getPassword())).isEqualTo("password");
  }

  @Test
  public void noDelegateInclinePair() {
    Path publicKeyPath = mock(Path.class);
    Path privateKeyPath = mock(Path.class);

    final FilesystemKeyPair filesystemKeyPair =
        new FilesystemKeyPair(publicKeyPath, privateKeyPath, null);

    assertThat(filesystemKeyPair.getPublicKey()).isNull();
    assertThat(filesystemKeyPair.getInlineKeypair()).isNull();
    assertThat(filesystemKeyPair.getPrivateKey()).isNull();
    assertThat(filesystemKeyPair.getPrivateKeyPath()).isSameAs(privateKeyPath);
    assertThat(filesystemKeyPair.getPublicKeyPath()).isSameAs(publicKeyPath);

    verifyZeroInteractions(keyEncryptor);
  }
}
