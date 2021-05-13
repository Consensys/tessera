package com.quorum.tessera.key.vault.aws;

import static com.quorum.tessera.config.util.EnvironmentVariables.AWS_ACCESS_KEY_ID;
import static com.quorum.tessera.config.util.EnvironmentVariables.AWS_SECRET_ACCESS_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.key.vault.KeyVaultService;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AWSKeyVaultServiceFactoryTest {

  private AWSKeyVaultServiceFactory awsKeyVaultServiceFactory;

  private Config config;

  private EnvironmentVariableProvider envProvider;

  @Before
  public void setUp() {
    this.config = mock(Config.class);
    this.envProvider = mock(EnvironmentVariableProvider.class);
    this.awsKeyVaultServiceFactory = new AWSKeyVaultServiceFactory();

    // required by the AWS SDK
    System.setProperty("aws.region", "a-region");
  }

  @After
  public void tearDown() {
    System.clearProperty("aws.region");
  }

  @Test(expected = NullPointerException.class)
  public void nullConfigThrowsException() {
    awsKeyVaultServiceFactory.create(null, envProvider);
  }

  @Test
  public void nullKeyConfigurationThrowsException() {
    when(envProvider.getEnv(anyString())).thenReturn("envVar");
    when(config.getKeys()).thenReturn(null);

    Throwable ex = catchThrowable(() -> awsKeyVaultServiceFactory.create(config, envProvider));

    assertThat(ex).isExactlyInstanceOf(ConfigException.class);
    assertThat(ex.getMessage())
        .contains("Trying to create AWS Secrets Manager connection but no configuration provided");
  }

  @Test
  public void nullKeyVaultConfigurationThrowsException() {
    when(envProvider.getEnv(anyString())).thenReturn("envVar");
    KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    when(keyConfiguration.getKeyVaultConfig(KeyVaultType.AWS)).thenReturn(Optional.empty());
    when(config.getKeys()).thenReturn(keyConfiguration);

    Throwable ex = catchThrowable(() -> awsKeyVaultServiceFactory.create(config, envProvider));

    assertThat(ex).isExactlyInstanceOf(ConfigException.class);
    assertThat(ex.getMessage())
        .contains("Trying to create AWS Secrets Manager connection but no configuration provided");
  }

  @Test
  public void onlyAWSAccessKeyIDEnvVarProvidedThrowsException() {
    Config config = mock(Config.class);

    when(envProvider.getEnv(AWS_ACCESS_KEY_ID)).thenReturn("id");
    Throwable ex = catchThrowable(() -> awsKeyVaultServiceFactory.create(config, envProvider));
    assertThat(ex).isInstanceOf(IncompleteAWSCredentialsException.class);
    assertThat(ex)
        .hasMessageContaining(
            "If using environment variables, both "
                + AWS_ACCESS_KEY_ID
                + " and "
                + AWS_SECRET_ACCESS_KEY
                + " must be set");
  }

  @Test
  public void onlyAWSSecretAccessKeyEnvVarProvidedThrowsException() {
    Config config = mock(Config.class);

    when(envProvider.getEnv(AWS_SECRET_ACCESS_KEY)).thenReturn("secret");
    Throwable ex = catchThrowable(() -> awsKeyVaultServiceFactory.create(config, envProvider));
    assertThat(ex).isInstanceOf(IncompleteAWSCredentialsException.class);
    assertThat(ex)
        .hasMessageContaining(
            "If using environment variables, both "
                + AWS_ACCESS_KEY_ID
                + " and "
                + AWS_SECRET_ACCESS_KEY
                + " must be set");
  }

  @Test
  public void envVarsAndKeyVaultConfigProvidedCreatesAWSKeyVaultService() {
    when(envProvider.getEnv(anyString())).thenReturn("envVar");
    KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);
    when(keyVaultConfig.getProperty("endpoint")).thenReturn(Optional.of("http://URL"));
    when(keyConfiguration.getKeyVaultConfig(KeyVaultType.AWS))
        .thenReturn(Optional.of(keyVaultConfig));
    when(config.getKeys()).thenReturn(keyConfiguration);

    KeyVaultService result = awsKeyVaultServiceFactory.create(config, envProvider);

    assertThat(result).isInstanceOf(AWSKeyVaultService.class);
  }

  @Test
  public void envVarsAndKeyVaultConfigWithNoEndpointProvidedCreatesAWSKeyVaultService() {
    when(envProvider.getEnv(anyString())).thenReturn("envVar");
    KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);
    when(keyConfiguration.getKeyVaultConfig(KeyVaultType.AWS))
        .thenReturn(Optional.of(keyVaultConfig));
    when(config.getKeys()).thenReturn(keyConfiguration);

    KeyVaultService result = awsKeyVaultServiceFactory.create(config, envProvider);

    assertThat(result).isInstanceOf(AWSKeyVaultService.class);
  }

  @Test
  public void invalidEndpointUrlThrowsException() {
    when(envProvider.getEnv(anyString())).thenReturn("envVar");
    KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);
    when(keyVaultConfig.getProperty("endpoint")).thenReturn(Optional.of("\\invalid"));
    when(keyConfiguration.getKeyVaultConfig(KeyVaultType.AWS))
        .thenReturn(Optional.of(keyVaultConfig));
    when(config.getKeys()).thenReturn(keyConfiguration);

    Throwable ex = catchThrowable(() -> awsKeyVaultServiceFactory.create(config, envProvider));

    assertThat(ex).isInstanceOf(ConfigException.class);
    assertThat(ex).hasMessageEndingWith("Invalid AWS endpoint URL provided");
  }

  @Test
  public void noSchemeEndpointUrlThrowsException() {
    when(envProvider.getEnv(anyString())).thenReturn("envVar");
    KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);
    when(keyVaultConfig.getProperty("endpoint")).thenReturn(Optional.of("noscheme"));
    when(keyConfiguration.getKeyVaultConfig(KeyVaultType.AWS))
        .thenReturn(Optional.of(keyVaultConfig));
    when(config.getKeys()).thenReturn(keyConfiguration);

    Throwable ex = catchThrowable(() -> awsKeyVaultServiceFactory.create(config, envProvider));

    assertThat(ex).isInstanceOf(ConfigException.class);
    assertThat(ex).hasMessageEndingWith("Invalid AWS endpoint URL provided - no scheme");
  }

  @Test
  public void getType() {
    assertThat(awsKeyVaultServiceFactory.getType()).isEqualTo(KeyVaultType.AWS);
  }
}
