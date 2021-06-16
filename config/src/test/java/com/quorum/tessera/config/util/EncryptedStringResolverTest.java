package com.quorum.tessera.config.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.jasypt.encryption.pbe.PBEStringCleanablePasswordEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EncryptedStringResolverTest {

  private EncryptedStringResolver encryptedStringResolver;

  private PBEStringCleanablePasswordEncryptor encryptor;

  private ConfigSecretReader configSecretReader;

  @Before
  public void beforeTest() {

    encryptor = new StandardPBEStringEncryptor();
    configSecretReader = mock(ConfigSecretReader.class);

    encryptedStringResolver = new EncryptedStringResolver(configSecretReader, encryptor);
  }

  @After
  public void afterTest() {
    verifyNoMoreInteractions(configSecretReader);
  }

  @Test
  public void testMarshall() {

    final String expectedValue = "password";

    when(configSecretReader.readSecretFromFile())
        .thenReturn(Optional.of(expectedValue.toCharArray()));

    assertThat(encryptedStringResolver.resolve("password")).isEqualTo(expectedValue);

    assertThat(encryptedStringResolver.resolve(null)).isNull();
  }

  @Test
  public void testUnMarshall() {

    final String normalPassword = "password";

    when(configSecretReader.readSecretFromFile())
        .thenReturn(Optional.of(normalPassword.toCharArray()));

    assertThat(encryptedStringResolver.resolve("password")).isEqualTo(normalPassword);

    when(configSecretReader.readSecretFromFile()).thenReturn(Optional.empty());
    when(configSecretReader.readSecretFromConsole()).thenReturn("quorum".toCharArray());

    assertThat(encryptedStringResolver.resolve("ENC(KLa6pRQpxI8Ez3Bo6D3cI6y13YYdntu7)"))
        .isEqualTo("password");

    assertThat(encryptedStringResolver.resolve(null)).isNull();

    verify(configSecretReader).readSecretFromFile();
    verify(configSecretReader).readSecretFromConsole();
  }

  @Test
  public void testUnMarshallWithUserInputSecret() {

    when(configSecretReader.readSecretFromConsole()).thenReturn("quorum".toCharArray());

    assertThat(encryptedStringResolver.resolve("ENC(KLa6pRQpxI8Ez3Bo6D3cI6y13YYdntu7)"))
        .isEqualTo("password");

    verify(configSecretReader).readSecretFromFile();
    verify(configSecretReader).readSecretFromConsole();
  }

  @Test
  public void testUnMarshallWrongPassword() {

    when(configSecretReader.readSecretFromConsole()).thenReturn("bogus".toCharArray());

    assertThatExceptionOfType(EncryptionOperationNotPossibleException.class)
        .isThrownBy(() -> encryptedStringResolver.resolve("ENC(KLa6pRQpxI8Ez3Bo6D3cI6y13YYdntu7)"));

    verify(configSecretReader).readSecretFromFile();
    verify(configSecretReader).readSecretFromConsole();
  }

  @Test
  public void defaultConstructor() {
    assertThat(new EncryptedStringResolver()).isNotNull();
  }
}
