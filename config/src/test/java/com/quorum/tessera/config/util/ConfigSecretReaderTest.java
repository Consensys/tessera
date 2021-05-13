package com.quorum.tessera.config.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ConfigSecretReaderTest {

  private String filePath;

  private ConfigSecretReader configSecretReader;

  private EnvironmentVariableProvider environmentVariableProvider;

  @Rule public TemporaryFolder tempDir = new TemporaryFolder();

  @Before
  public void beforeTest() {
    environmentVariableProvider = mock(EnvironmentVariableProvider.class);
    configSecretReader = new ConfigSecretReader(environmentVariableProvider);
    filePath = getClass().getResource("/key.secret").getPath();
  }

  @After
  public void afterTest() {
    verifyNoMoreInteractions(environmentVariableProvider);
  }

  @Test
  public void testReadSecret() {

    when(environmentVariableProvider.getEnv(EnvironmentVariables.CONFIG_SECRET_PATH))
        .thenReturn(filePath);
    when(environmentVariableProvider.hasEnv(EnvironmentVariables.CONFIG_SECRET_PATH))
        .thenReturn(true);
    Optional<char[]> secret = configSecretReader.readSecretFromFile();

    assertThat(secret).isPresent();
    assertThat(secret.get()).isEqualTo("quorum".toCharArray());

    verify(environmentVariableProvider).hasEnv(EnvironmentVariables.CONFIG_SECRET_PATH);
    verify(environmentVariableProvider).getEnv(EnvironmentVariables.CONFIG_SECRET_PATH);
  }

  @Test
  public void testNotAbleToReadSecret() {
    when(environmentVariableProvider.hasEnv(EnvironmentVariables.CONFIG_SECRET_PATH))
        .thenReturn(true);
    when(environmentVariableProvider.getEnv(EnvironmentVariables.CONFIG_SECRET_PATH))
        .thenReturn("not-existed");

    assertThat(configSecretReader.readSecretFromFile()).isEmpty();

    verify(environmentVariableProvider).hasEnv(EnvironmentVariables.CONFIG_SECRET_PATH);
    verify(environmentVariableProvider).getEnv(EnvironmentVariables.CONFIG_SECRET_PATH);
  }

  @Test
  public void envNotSet() {
    when(environmentVariableProvider.hasEnv(EnvironmentVariables.CONFIG_SECRET_PATH))
        .thenReturn(false);
    assertThat(configSecretReader.readSecretFromFile()).isEmpty();
    verify(environmentVariableProvider).hasEnv(EnvironmentVariables.CONFIG_SECRET_PATH);
  }

  @Test
  public void testReadFromConsole() {
    ByteArrayInputStream in = new ByteArrayInputStream("password".getBytes());
    System.setIn(in);
    assertThat(configSecretReader.readSecretFromConsole()).isEqualTo("password".toCharArray());

    System.setIn(System.in);
    assertThat(configSecretReader.readSecretFromConsole()).isEqualTo("".toCharArray());
  }

  @Test
  public void testReadException() throws IOException {

    when(environmentVariableProvider.hasEnv(EnvironmentVariables.CONFIG_SECRET_PATH))
        .thenReturn(true);
    final File tempFile = tempDir.newFile("key.secret");
    tempFile.setReadable(false);

    when(environmentVariableProvider.getEnv(EnvironmentVariables.CONFIG_SECRET_PATH))
        .thenReturn(tempFile.getAbsolutePath());

    assertThat(configSecretReader.readSecretFromFile()).isEmpty();

    verify(environmentVariableProvider).hasEnv(EnvironmentVariables.CONFIG_SECRET_PATH);
    verify(environmentVariableProvider).getEnv(EnvironmentVariables.CONFIG_SECRET_PATH);
  }
}
