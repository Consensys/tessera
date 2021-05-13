package com.quorum.tessera.config.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.ByteArrayInputStream;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

public class EncryptedStringResolverTest {

  private EncryptedStringResolver resolver;

  @Rule public final EnvironmentVariables envVariables = new EnvironmentVariables();

  @Before
  public void init() {

    final String filePath = getClass().getResource("/key.secret").getPath();
    envVariables.set(
        com.quorum.tessera.config.util.EnvironmentVariables.CONFIG_SECRET_PATH, filePath);
    resolver = new EncryptedStringResolver();
  }

  @Test
  public void testMarshall() {

    final String expectedValue = "password";

    assertThat(resolver.resolve("password")).isEqualTo(expectedValue);

    assertThat(resolver.resolve(null)).isNull();
  }

  @Test
  public void testUnMarshall() {

    String normalPassword = "password";

    assertThat(resolver.resolve("password")).isEqualTo(normalPassword);

    assertThat(resolver.resolve("ENC(KLa6pRQpxI8Ez3Bo6D3cI6y13YYdntu7)")).isEqualTo("password");

    assertThat(resolver.resolve(null)).isNull();
  }

  @Test
  public void testUnMarshallWithUserInputSecret() {

    envVariables.clear(com.quorum.tessera.config.util.EnvironmentVariables.CONFIG_SECRET_PATH);

    resolver = new EncryptedStringResolver();

    ByteArrayInputStream in =
        new ByteArrayInputStream(("quorum" + System.lineSeparator() + "quorum").getBytes());
    System.setIn(in);

    assertThat(resolver.resolve("ENC(KLa6pRQpxI8Ez3Bo6D3cI6y13YYdntu7)")).isEqualTo("password");

    System.setIn(System.in);
  }

  @Test
  public void testUnMarshallWrongPassword() {

    envVariables.clear(com.quorum.tessera.config.util.EnvironmentVariables.CONFIG_SECRET_PATH);

    resolver = new EncryptedStringResolver();

    ByteArrayInputStream in =
        new ByteArrayInputStream(("bogus" + System.lineSeparator() + "bogus").getBytes());
    System.setIn(in);

    assertThatExceptionOfType(EncryptionOperationNotPossibleException.class)
        .isThrownBy(() -> resolver.resolve("ENC(KLa6pRQpxI8Ez3Bo6D3cI6y13YYdntu7)"));

    System.setIn(System.in);
  }
}
