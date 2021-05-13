package com.quorum.tessera.cli.keypassresolver;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.encryption.EncryptorException;
import com.quorum.tessera.encryption.PrivateKey;
import com.quorum.tessera.passwords.PasswordReader;
import com.quorum.tessera.passwords.PasswordReaderFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

public class CliKeyPasswordResolverTest {

  @Rule public SystemOutRule systemOutRule = new SystemOutRule().enableLog();

  private PasswordReader passwordReader;

  private CliKeyPasswordResolver cliKeyPasswordResolver;

  private KeyEncryptor keyEncryptor;

  @Before
  public void init() {
    this.passwordReader = mock(PasswordReader.class);
    this.keyEncryptor = mock(KeyEncryptor.class);
    this.cliKeyPasswordResolver = new CliKeyPasswordResolver(passwordReader);
  }

  @Test
  public void defaultConstructorCreatesReaderInstanceFromFactory() {

    try (var staticPasswordReaderFactory = mockStatic(PasswordReaderFactory.class)) {
      staticPasswordReaderFactory.when(PasswordReaderFactory::create).thenReturn(passwordReader);
      final CliKeyPasswordResolver resolver = new CliKeyPasswordResolver();
      assertThat(resolver).isNotNull();
      staticPasswordReaderFactory.verify(PasswordReaderFactory::create);
    }
  }

  @Test
  public void emptyPasswordsReturnsSameKeys() {

    // null paths since we won't actually be reading them
    final KeyData keypair = new KeyData();
    final KeyConfiguration keyConfig =
        new KeyConfiguration(null, emptyList(), singletonList(keypair), null, null);
    final Config config = new Config();
    config.setKeys(keyConfig);

    this.cliKeyPasswordResolver.resolveKeyPasswords(config);

    assertThat(keyConfig.getKeyData()).hasSize(1);
    final KeyData returned = keyConfig.getKeyData().get(0);

    // passwords are always non-null, set to empty string if not present or not needed
    assertThat(returned.getPassword()).isNull();
    assertThat(returned).isSameAs(keypair);
  }

  @Test
  public void noPasswordsReturnsSameKeys() {

    // null paths since we won't actually be reading them
    final KeyData keypair = new KeyData();
    final KeyConfiguration keyConfig =
        new KeyConfiguration(null, null, singletonList(keypair), null, null);
    final Config config = new Config();
    config.setKeys(keyConfig);

    this.cliKeyPasswordResolver.resolveKeyPasswords(config);

    assertThat(keyConfig.getKeyData()).hasSize(1);
    final KeyData returned = keyConfig.getKeyData().get(0);

    // passwords are always non-null, set to empty string if not present or not needed
    assertThat(returned.getPassword()).isNull();
    assertThat(returned).isSameAs(keypair);
  }

  @Test
  public void passwordsAssignedToKeys() {

    // null paths since we won't actually be reading them
    final KeyData keypair = new KeyData();
    final KeyConfiguration keyConfig =
        new KeyConfiguration(
            null, singletonList("passwordsAssignedToKeys"), singletonList(keypair), null, null);
    final Config config = new Config();
    config.setKeys(keyConfig);

    this.cliKeyPasswordResolver.resolveKeyPasswords(config);

    assertThat(keyConfig.getKeyData()).hasSize(1);
    final KeyData returned = keyConfig.getKeyData().get(0);
    assertThat(String.valueOf(returned.getPassword())).isEqualTo("passwordsAssignedToKeys");
  }

  @Test
  public void unreadablePasswordFileGivesNoPasswords() throws IOException {

    final Path passes = Files.createTempDirectory("testdirectory").resolve("nonexistantfile.txt");

    final KeyData keypair = new KeyData();

    final KeyConfiguration keyConfig =
        new KeyConfiguration(passes, null, singletonList(keypair), null, null);
    final Config config = new Config();
    config.setKeys(keyConfig);

    this.cliKeyPasswordResolver.resolveKeyPasswords(config);

    assertThat(keyConfig.getKeyData()).hasSize(1);
    final KeyData returned = keyConfig.getKeyData().get(0);
    assertThat(returned.getPassword()).isNull();
  }

  @Test
  public void readablePasswordFileAssignsPasswords() throws IOException {

    final Path passes = Files.createTempDirectory("testdirectory").resolve("passwords.txt");
    Files.write(passes, "q".getBytes());

    final KeyData keypair = new KeyData();
    final KeyConfiguration keyConfig =
        new KeyConfiguration(passes, null, singletonList(keypair), null, null);
    final Config config = new Config();
    config.setKeys(keyConfig);

    this.cliKeyPasswordResolver.resolveKeyPasswords(config);

    assertThat(keyConfig.getKeyData()).hasSize(1);
    final KeyData returned = keyConfig.getKeyData().get(0);
    assertThat(String.valueOf(returned.getPassword())).isEqualTo("q");
  }

  @Test
  public void nullKeyConfigReturns() {
    final Throwable throwable =
        catchThrowable(() -> this.cliKeyPasswordResolver.resolveKeyPasswords(new Config()));

    assertThat(throwable).isNull();
  }

  @Test
  public void gettingPasswordForNonInlineOrFileSystemKeyReturns() {

    final KeyData keyPair = new KeyData();
    keyPair.setPublicKey("public");
    keyPair.setPrivateKey("private");

    this.cliKeyPasswordResolver.getSingleKeyPassword(0, keyPair, keyEncryptor);

    assertThat(keyPair.getPassword()).isNullOrEmpty();
  }

  @Test
  public void nullInlineKeyDoesntReadPassword() {

    KeyData keyData = new KeyData();
    this.cliKeyPasswordResolver.getSingleKeyPassword(0, keyData, keyEncryptor);

    assertThat(keyData.getPassword()).isNullOrEmpty();
  }

  @Test
  public void unlockedKeyDoesntReadPassword() {
    final KeyDataConfig privKeyDataConfig =
        new KeyDataConfig(
            new PrivateKeyData(
                "Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=", null, null, null, null),
            PrivateKeyType.UNLOCKED);

    KeyData keyData = new KeyData();
    keyData.setPublicKey("public");
    keyData.setConfig(privKeyDataConfig);

    this.cliKeyPasswordResolver.getSingleKeyPassword(0, keyData, keyEncryptor);

    assertThat(keyData.getPassword()).isNullOrEmpty();
  }

  @Test
  public void lockedKeyWithEmptyPasswordRequestsPassword() {

    when(passwordReader.readPasswordFromConsole()).thenReturn("a".toCharArray());

    final KeyDataConfig privKeyDataConfig = mock(KeyDataConfig.class);
    when(privKeyDataConfig.getType()).thenReturn(PrivateKeyType.LOCKED);

    PrivateKeyData privateKeyData = mock(PrivateKeyData.class);
    when(privKeyDataConfig.getPrivateKeyData()).thenReturn(privateKeyData);

    KeyData keyPair = new KeyData();
    keyPair.setPassword(new char[0]);
    keyPair.setPublicKey("public");
    keyPair.setConfig(privKeyDataConfig);

    when(keyEncryptor.decryptPrivateKey(any(), any())).thenThrow(new EncryptorException(""));

    this.cliKeyPasswordResolver.getSingleKeyPassword(0, keyPair, keyEncryptor);

    assertThat(systemOutRule.getLog())
        .containsOnlyOnce(
            "Password for key[0] missing or invalid.\nAttempt 1 of 2. Enter a password for the key");
  }

  @Test
  public void lockedKeyWithInvalidPasswordRequestsPassword() {
    when(passwordReader.readPasswordFromConsole()).thenReturn("a".toCharArray());

    final char[] validPassword = "a".toCharArray();
    final char[] invalidPassword = "invalidPassword".toCharArray();

    byte[] privateKeyBytes =
        Base64.getDecoder().decode("w+itzh2vfuGjiGYEVJtqpiJVUmI5vGUK4CzMErxa+GY=");
    final PrivateKey unlockedKey = PrivateKey.from(privateKeyBytes);

    final KeyDataConfig privKeyDataConfig =
        new KeyDataConfig(
            new PrivateKeyData(
                "Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=",
                "yb7M8aRJzgxoJM2NecAPcmSVWDW1tRjv",
                "MIqkFlgR2BWEpx2U0rObGg==",
                "Gtvp1t6XZEiFVyaE/LHiP1+yvOIBBoiOL+bKeqcKgpiNt4j1oDDoqCC47UJpmQRC",
                new ArgonOptions("i", 10, 1048576, 4)),
            PrivateKeyType.LOCKED);

    KeyEncryptor keyEncryptor = mock(KeyEncryptor.class);
    when(keyEncryptor.decryptPrivateKey(any(PrivateKeyData.class), eq(invalidPassword)))
        .thenThrow(new EncryptorException("decrypt failed"));
    when(keyEncryptor.decryptPrivateKey(any(PrivateKeyData.class), eq(validPassword)))
        .thenReturn(unlockedKey);

    KeyData keyPair = new KeyData();
    keyPair.setPublicKey("public");
    keyPair.setConfig(privKeyDataConfig);

    this.cliKeyPasswordResolver.getSingleKeyPassword(0, keyPair, keyEncryptor);

    assertThat(systemOutRule.getLog())
        .containsOnlyOnce(
            "Password for key[0] missing or invalid.\nAttempt 1 of 2. Enter a password for the key");
  }

  @Test
  public void lockedKeyWithValidPasswordDoesNotRequestPassword() {
    final char[] validPassword = "somepass".toCharArray();

    final KeyDataConfig privKeyDataConfig =
        new KeyDataConfig(
            new PrivateKeyData(
                "Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=",
                "yb7M8aRJzgxoJM2NecAPcmSVWDW1tRjv",
                "MIqkFlgR2BWEpx2U0rObGg==",
                "Gtvp1t6XZEiFVyaE/LHiP1+yvOIBBoiOL+bKeqcKgpiNt4j1oDDoqCC47UJpmQRC",
                new ArgonOptions("i", 10, 1048576, 4)),
            PrivateKeyType.LOCKED);

    final KeyData keyPair = new KeyData();
    keyPair.setPassword(validPassword);
    keyPair.setConfig(privKeyDataConfig);
    keyPair.setPublicKey("public");

    when(keyEncryptor.decryptPrivateKey(any(), any())).thenReturn(mock(PrivateKey.class));

    this.cliKeyPasswordResolver.getSingleKeyPassword(0, keyPair, keyEncryptor);

    verifyZeroInteractions(passwordReader);
  }

  //    @Test
  //    public void invalidRequestedPasswordRerequests() {
  //
  //        when(passwordReader.readPasswordFromConsole()).thenReturn("invalid", "a");
  //
  //        PrivateKeyData privateKeyData = mock(PrivateKeyData.class);
  //
  //        final KeyDataConfig privKeyDataConfig = mock(KeyDataConfig.class);
  //        when(privKeyDataConfig.getPrivateKeyData()).thenReturn(privateKeyData);
  //        when(privKeyDataConfig.getType()).thenReturn(PrivateKeyType.LOCKED);
  //
  //
  //        KeyData keyPair = new KeyData();
  //        keyPair.setPublicKey("public");
  //        keyPair.setConfig(privKeyDataConfig);
  //        keyPair.setPassword("invalidPassword");
  //
  //
  //        this.cliKeyPasswordResolver.getSingleKeyPassword(0, keyPair);
  //
  //        // work around for checking string appears twice in message
  //        assertThat(systemOutRule.getLog())
  //            .containsOnlyOnce(
  //                "Password for key[0] missing or invalid.\nAttempt 1 of 2. Enter a password for
  // the key")
  //            .containsOnlyOnce(
  //                "Password for key[0] missing or invalid.\nAttempt 2 of 2. Enter a password for
  // the key");
  //    }

  @Test
  public void lockedKeyWithEncrptionErrorP() {
    when(passwordReader.readPasswordFromConsole()).thenReturn("a".toCharArray());

    final KeyDataConfig privKeyDataConfig = mock(KeyDataConfig.class);
    when(privKeyDataConfig.getType()).thenReturn(PrivateKeyType.LOCKED);
    PrivateKeyData privateKeyData = mock(PrivateKeyData.class);
    when(privKeyDataConfig.getPrivateKeyData()).thenReturn(privateKeyData);

    final KeyData keyPair = new KeyData();
    keyPair.setPublicKey("public");
    keyPair.setConfig(privKeyDataConfig);
    keyPair.setPrivateKey("NACL_FAILURE");

    this.cliKeyPasswordResolver.getSingleKeyPassword(0, keyPair, keyEncryptor);

    assertThat(systemOutRule.getLog())
        .containsOnlyOnce(
            "Password for key[0] missing or invalid.\nAttempt 1 of 2. Enter a password for the key");
  }
}
