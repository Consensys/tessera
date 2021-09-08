package com.quorum.tessera.config.cli;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.quorum.tessera.cli.CliException;
import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.encryption.PrivateKey;
import com.quorum.tessera.passwords.PasswordReader;
import jakarta.xml.bind.UnmarshalException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class KeyUpdateCommandTest {

  private KeyUpdateCommand command;

  private KeyEncryptorFactory keyEncryptorFactory;

  private KeyEncryptor keyEncryptor;

  private PasswordReader passwordReader;

  @Before
  public void onSetup() {
    keyEncryptorFactory = mock(KeyEncryptorFactory.class);
    keyEncryptor = mock(KeyEncryptor.class);
    passwordReader = mock(PasswordReader.class);

    when(keyEncryptorFactory.create(any())).thenReturn(keyEncryptor);
    when(passwordReader.requestUserPassword()).thenReturn("newPassword".toCharArray());

    command = new KeyUpdateCommand(keyEncryptorFactory, passwordReader);
    command.keyEncryptor = keyEncryptor;
  }

  @After
  public void onTeardown() {
    verifyNoMoreInteractions(keyEncryptorFactory, keyEncryptor, passwordReader);
  }

  // Argon Option tests
  // TODO(cjh) re-enable this once the tests have become more integration-based (i.e. I think
  // defaults will only be
  //  set when creating a command line object and calling parseArgs or execute
  @Ignore
  @Test
  public void noArgonOptionsGivenHasDefaults() throws Exception {
    //        final CommandLine commandLine = new DefaultParser().parse(options, new String[] {});
    //
    //        final ArgonOptions argonOptions = KeyUpdateParser.argonOptions(commandLine);
    //
    //        assertThat(argonOptions.getAlgorithm()).isEqualTo("i");
    //        assertThat(argonOptions.getParallelism()).isEqualTo(4);
    //        assertThat(argonOptions.getMemory()).isEqualTo(1048576);
    //        assertThat(argonOptions.getIterations()).isEqualTo(10);
  }

  @Test
  public void argonOptionsGivenHasOverrides() {
    command.algorithm = "d";
    command.memory = 100;
    command.iterations = 100;
    command.parallelism = 100;

    final ArgonOptions argonOptions = command.argonOptions();

    assertThat(argonOptions.getAlgorithm()).isEqualTo("d");
    assertThat(argonOptions.getParallelism()).isEqualTo(100);
    assertThat(argonOptions.getMemory()).isEqualTo(100);
    assertThat(argonOptions.getIterations()).isEqualTo(100);
  }

  @Test
  public void argonOptionsInvalidTypeThrowsException() {
    command.memory = 100;
    command.iterations = 100;
    command.parallelism = 100;

    command.algorithm = "i";
    command.argonOptions();

    command.algorithm = "d";
    command.argonOptions();

    command.algorithm = "id";
    command.argonOptions();

    command.algorithm = "invalid";
    Throwable ex = catchThrowable(() -> command.argonOptions());

    assertThat(ex).isInstanceOf(CliException.class);
    assertThat(ex).hasMessage(KeyUpdateCommand.invalidArgonAlgorithmMsg);
  }

  // Password reading tests
  @Test
  public void inlinePasswordParsed() throws IOException {
    command.password = "pass";

    final List<char[]> passwords = command.passwords();

    assertThat(passwords).isNotNull().hasSize(1).containsExactly("pass".toCharArray());
  }

  @Test
  public void passwordFileParsedAndRead() throws IOException {
    final Path passwordFile = Files.createTempFile("passwords", ".txt");
    Files.write(passwordFile, "passwordInsideFile\nsecondPassword".getBytes());

    command.passwordFile = passwordFile;

    final List<char[]> passwords = command.passwords();

    assertThat(passwords)
        .isNotNull()
        .hasSize(2)
        .containsExactly("passwordInsideFile".toCharArray(), "secondPassword".toCharArray());
  }

  @Test
  public void passwordFileThrowsErrorIfCantBeRead() {
    command.passwordFile = Paths.get("/tmp/passwords.txt");

    final Throwable throwable = catchThrowable(() -> command.passwords());

    assertThat(throwable).isNotNull().isInstanceOf(IOException.class);
  }

  @Test
  public void emptyListGivenForNoPasswords() throws IOException {
    final List<char[]> passwords = command.passwords();

    assertThat(passwords).isNotNull().isEmpty();
  }

  // key file tests
  // TODO(cjh) re-enable this once the tests have become more integration-based (i.e. required
  // fields can be tested
  //  when creating a command line object and calling parseArgs or execute
  @Ignore
  @Test
  public void noPrivateKeyGivenThrowsError() {
    //        final Throwable throwable = catchThrowable(() ->
    // KeyUpdateParser.privateKeyPath(commandLine));
    //
    //        assertThat(throwable)
    //            .isInstanceOf(IllegalArgumentException.class)
    //            .hasMessage("Private key path cannot be null when updating key password");
  }

  @Test
  public void cantReadPrivateKeyThrowsError() {
    command.privateKeyPath = Paths.get("/tmp/nonexisting.txt");

    final Throwable throwable = catchThrowable(() -> command.privateKeyPath());

    assertThat(throwable).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void privateKeyExistsReturnsPath() throws IOException {
    final Path key = Files.createTempFile("key", ".key");

    command.privateKeyPath = key;

    final Path path = command.privateKeyPath();

    assertThat(path).isEqualTo(key);
  }

  // key fetching tests
  @Test
  public void unlockedKeyReturnedProperly() {
    final KeyDataConfig kdc =
        new KeyDataConfig(
            new PrivateKeyData(
                "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=", null, null, null, null),
            PrivateKeyType.UNLOCKED);

    final PrivateKey key = command.getExistingKey(kdc, emptyList());

    String encodedKeyValue = Base64.getEncoder().encodeToString(key.getKeyBytes());

    assertThat(encodedKeyValue).isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
  }

  @Test
  public void lockedKeyFailsWithNoPasswordsMatching() {

    final KeyDataConfig kdc =
        new KeyDataConfig(
            new PrivateKeyData(
                null,
                "dwixVoY+pOI2FMuu4k0jLqN/naQiTzWe",
                "JoPVq9G6NdOb+Ugv+HnUeA==",
                "6Jd/MXn29fk6jcrFYGPb75l7sDJae06I3Y1Op+bZSZqlYXsMpa/8lLE29H0sX3yw",
                new ArgonOptions("id", 1, 1024, 1)),
            PrivateKeyType.LOCKED);

    final Throwable throwable =
        catchThrowable(() -> command.getExistingKey(kdc, singletonList("wrong".toCharArray())));

    assertThat(throwable)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Locked key but no valid password given");

    verify(keyEncryptor).decryptPrivateKey(kdc.getPrivateKeyData(), "wrong".toCharArray());
  }

  @Test
  public void lockedKeySucceedsWithPasswordsMatching() {
    PrivateKeyData privateKeyData =
        new PrivateKeyData(
            null,
            "dwixVoY+pOI2FMuu4k0jLqN/naQiTzWe",
            "JoPVq9G6NdOb+Ugv+HnUeA==",
            "6Jd/MXn29fk6jcrFYGPb75l7sDJae06I3Y1Op+bZSZqlYXsMpa/8lLE29H0sX3yw",
            new ArgonOptions("id", 1, 1024, 1));

    final KeyDataConfig kdc =
        new KeyDataConfig(
            new PrivateKeyData(
                null,
                "dwixVoY+pOI2FMuu4k0jLqN/naQiTzWe",
                "JoPVq9G6NdOb+Ugv+HnUeA==",
                "6Jd/MXn29fk6jcrFYGPb75l7sDJae06I3Y1Op+bZSZqlYXsMpa/8lLE29H0sX3yw",
                new ArgonOptions("id", 1, 1024, 1)),
            PrivateKeyType.LOCKED);

    PrivateKey privateKey = mock(PrivateKey.class);
    when(privateKey.getKeyBytes()).thenReturn("SUCCESS".getBytes());
    when(keyEncryptor.decryptPrivateKey(privateKeyData, "testpassword".toCharArray()))
        .thenReturn(privateKey);

    final PrivateKey result =
        command.getExistingKey(kdc, singletonList("testpassword".toCharArray()));

    assertThat(result.getKeyBytes()).isEqualTo("SUCCESS".getBytes());

    verify(keyEncryptor).decryptPrivateKey(privateKeyData, "testpassword".toCharArray());
  }

  @Test
  public void loadingMalformedKeyfileThrowsError() throws Exception {
    final Path key = Files.createTempFile("key", ".key");
    Files.write(key, "BAD JSON DATA".getBytes());

    command.privateKeyPath = key;

    addEmptyEncryptorConfigToCommand();
    addDefaultArgonConfigToCommand();

    final Throwable throwable = catchThrowable(() -> command.call());

    assertThat(throwable)
        .isInstanceOf(ConfigException.class)
        .hasCauseExactlyInstanceOf(UnmarshalException.class);

    verify(keyEncryptorFactory).create(any());
  }

  @Test
  public void keyGetsUpdated() throws Exception {
    final KeyDataConfig startingKey =
        JaxbUtil.unmarshal(
            getClass().getResourceAsStream("/lockedprivatekey.json"), KeyDataConfig.class);

    final Path key = Files.createTempFile("key", ".key");
    Files.write(key, JaxbUtil.marshalToString(startingKey).getBytes());

    command.privateKeyPath = key;
    command.password = "testpassword";

    addDefaultArgonConfigToCommand();
    addEmptyEncryptorConfigToCommand();

    PrivateKey privatekey = mock(PrivateKey.class);
    when(keyEncryptor.decryptPrivateKey(any(PrivateKeyData.class), any())).thenReturn(privatekey);

    PrivateKeyData privateKeyData = mock(PrivateKeyData.class);

    when(keyEncryptor.encryptPrivateKey(any(PrivateKey.class), any(), any(ArgonOptions.class)))
        .thenReturn(privateKeyData);

    command.call();

    final KeyDataConfig endingKey =
        JaxbUtil.unmarshal(Files.newInputStream(key), KeyDataConfig.class);

    assertThat(endingKey.getSbox()).isNotEqualTo(startingKey.getSbox());
    assertThat(endingKey.getSnonce()).isNotEqualTo(startingKey.getSnonce());
    assertThat(endingKey.getAsalt()).isNotEqualTo(startingKey.getAsalt());

    verify(keyEncryptorFactory).create(any());
    verify(keyEncryptor).decryptPrivateKey(any(PrivateKeyData.class), any());
    verify(keyEncryptor).encryptPrivateKey(any(PrivateKey.class), any(), any(ArgonOptions.class));
    verify(passwordReader).requestUserPassword();
  }

  @Test
  public void keyGetsUpdatedUsingEncryptorOptions() throws Exception {
    final KeyDataConfig startingKey =
        JaxbUtil.unmarshal(
            getClass().getResourceAsStream("/lockedprivatekey.json"), KeyDataConfig.class);

    final Path key = Files.createTempFile("key", ".key");
    Files.write(key, JaxbUtil.marshalToString(startingKey).getBytes());

    command.privateKeyPath = key;
    command.password = "testpassword";

    addDefaultArgonConfigToCommand();
    addEncryptorOptionsToCommand();

    PrivateKey privatekey = mock(PrivateKey.class);
    when(keyEncryptor.decryptPrivateKey(any(PrivateKeyData.class), any())).thenReturn(privatekey);

    PrivateKeyData privateKeyData = mock(PrivateKeyData.class);

    when(keyEncryptor.encryptPrivateKey(any(PrivateKey.class), any(), any(ArgonOptions.class)))
        .thenReturn(privateKeyData);

    command.call();

    final KeyDataConfig endingKey =
        JaxbUtil.unmarshal(Files.newInputStream(key), KeyDataConfig.class);

    assertThat(endingKey.getSbox()).isNotEqualTo(startingKey.getSbox());
    assertThat(endingKey.getSnonce()).isNotEqualTo(startingKey.getSnonce());
    assertThat(endingKey.getAsalt()).isNotEqualTo(startingKey.getAsalt());

    verify(keyEncryptorFactory).create(any());
    verify(keyEncryptor).decryptPrivateKey(any(PrivateKeyData.class), any());
    verify(keyEncryptor).encryptPrivateKey(any(PrivateKey.class), any(), any(ArgonOptions.class));
    verify(passwordReader).requestUserPassword();
  }

  @Test
  public void keyGetsUpdatedToNoPassword() throws Exception {
    final KeyDataConfig startingKey =
        JaxbUtil.unmarshal(
            getClass().getResourceAsStream("/lockedprivatekey.json"), KeyDataConfig.class);

    when(passwordReader.requestUserPassword()).thenReturn("".toCharArray());

    final Path key = Files.createTempFile("key", ".key");
    Files.write(key, JaxbUtil.marshalToString(startingKey).getBytes());

    command.privateKeyPath = key;
    command.password = "testpassword";

    addDefaultArgonConfigToCommand();
    addEmptyEncryptorConfigToCommand();

    byte[] privateKeyData = "SOME PRIVATE DATA".getBytes();
    PrivateKey privateKey = PrivateKey.from(privateKeyData);
    when(keyEncryptor.decryptPrivateKey(any(PrivateKeyData.class), any())).thenReturn(privateKey);

    command.call();

    final KeyDataConfig endingKey =
        JaxbUtil.unmarshal(Files.newInputStream(key), KeyDataConfig.class);

    assertThat(endingKey.getSbox()).isNotEqualTo(startingKey.getSbox());
    assertThat(endingKey.getSnonce()).isNotEqualTo(startingKey.getSnonce());
    assertThat(endingKey.getAsalt()).isNotEqualTo(startingKey.getAsalt());
    assertThat(endingKey.getPrivateKeyData().getValue())
        .isEqualTo(Base64.getEncoder().encodeToString(privateKeyData));

    verify(keyEncryptorFactory).create(any());
    verify(keyEncryptor).decryptPrivateKey(any(PrivateKeyData.class), any());
    verify(keyEncryptor, never())
        .encryptPrivateKey(any(PrivateKey.class), any(), any(ArgonOptions.class));
    verify(passwordReader).requestUserPassword();
  }

  private void addEmptyEncryptorConfigToCommand() {
    final Config config = new Config();
    final EncryptorConfig encryptorConfig = new EncryptorConfig();
    config.setEncryptor(encryptorConfig);
    command.config = config;
  }

  private void addEncryptorOptionsToCommand() {
    command.encryptorOptions = new EncryptorOptions();
  }

  private void addDefaultArgonConfigToCommand() {
    command.algorithm = "d";
    command.memory = 100;
    command.iterations = 100;
    command.parallelism = 100;
  }
}
