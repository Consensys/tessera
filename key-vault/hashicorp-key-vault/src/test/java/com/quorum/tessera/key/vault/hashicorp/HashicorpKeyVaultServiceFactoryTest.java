package com.quorum.tessera.key.vault.hashicorp;

import static com.quorum.tessera.config.util.EnvironmentVariables.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.key.vault.KeyVaultService;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.support.ClientOptions;
import org.springframework.vault.support.SslConfiguration;

public class HashicorpKeyVaultServiceFactoryTest {

  private HashicorpKeyVaultServiceFactory keyVaultServiceFactory;

  private Config config;

  private EnvironmentVariableProvider envProvider;

  private HashicorpKeyVaultServiceFactoryUtil keyVaultServiceFactoryUtil;

  private String noCredentialsExceptionMsg =
      "Environment variables must be set to authenticate with Hashicorp Vault.  Set the "
          + HASHICORP_ROLE_ID
          + " and "
          + HASHICORP_SECRET_ID
          + " environment variables if using the AppRole authentication method.  Set the "
          + HASHICORP_TOKEN
          + " environment variable if using another authentication method.";

  private String approleCredentialsExceptionMsg =
      "Only one of the "
          + HASHICORP_ROLE_ID
          + " and "
          + HASHICORP_SECRET_ID
          + " environment variables to authenticate with Hashicorp Vault using the AppRole method has been set";

  @Before
  public void setUp() {
    this.keyVaultServiceFactory = new HashicorpKeyVaultServiceFactory();
    this.config = mock(Config.class);
    this.envProvider = mock(EnvironmentVariableProvider.class);
    this.keyVaultServiceFactoryUtil = mock(HashicorpKeyVaultServiceFactoryUtil.class);
  }

  @Test(expected = NullPointerException.class)
  public void nullConfigThrowsException() {
    keyVaultServiceFactory.create(null, envProvider);
  }

  @Test(expected = NullPointerException.class)
  public void nullEnvVarProviderThrowsException() {
    keyVaultServiceFactory.create(config, null);
  }

  @Test
  public void getType() {
    assertThat(keyVaultServiceFactory.getType()).isEqualTo(KeyVaultType.HASHICORP);
  }

  @Test
  public void exceptionThrownIfNoAuthEnvVarsSet() {
    when(envProvider.getEnv(HASHICORP_ROLE_ID)).thenReturn(null);
    when(envProvider.getEnv(HASHICORP_SECRET_ID)).thenReturn(null);
    when(envProvider.getEnv(HASHICORP_TOKEN)).thenReturn(null);

    Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider));

    assertThat(ex).isInstanceOf(HashicorpCredentialNotSetException.class);
    assertThat(ex).hasMessage(noCredentialsExceptionMsg);
  }

  @Test
  public void exceptionThrownIfOnlyRoleIdAuthEnvVarSet() {
    when(envProvider.getEnv(HASHICORP_ROLE_ID)).thenReturn("role-id");
    when(envProvider.getEnv(HASHICORP_SECRET_ID)).thenReturn(null);
    when(envProvider.getEnv(HASHICORP_TOKEN)).thenReturn(null);

    Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider));

    assertThat(ex).isInstanceOf(HashicorpCredentialNotSetException.class);
    assertThat(ex).hasMessage(approleCredentialsExceptionMsg);
  }

  @Test
  public void exceptionThrownIfOnlySecretIdAuthEnvVarSet() {
    when(envProvider.getEnv(HASHICORP_ROLE_ID)).thenReturn(null);
    when(envProvider.getEnv(HASHICORP_SECRET_ID)).thenReturn("secret-id");
    when(envProvider.getEnv(HASHICORP_TOKEN)).thenReturn(null);

    Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider));

    assertThat(ex).isInstanceOf(HashicorpCredentialNotSetException.class);
    assertThat(ex).hasMessage(approleCredentialsExceptionMsg);
  }

  @Test
  public void exceptionThrownIfOnlyRoleIdAndTokenAuthEnvVarsSet() {
    when(envProvider.getEnv(HASHICORP_ROLE_ID)).thenReturn("role-id");
    when(envProvider.getEnv(HASHICORP_SECRET_ID)).thenReturn(null);
    when(envProvider.getEnv(HASHICORP_TOKEN)).thenReturn("token");

    Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider));

    assertThat(ex).isInstanceOf(HashicorpCredentialNotSetException.class);
    assertThat(ex).hasMessage(approleCredentialsExceptionMsg);
  }

  @Test
  public void exceptionThrownIfOnlySecretIdAndTokenAuthEnvVarsSet() {
    when(envProvider.getEnv(HASHICORP_ROLE_ID)).thenReturn(null);
    when(envProvider.getEnv(HASHICORP_SECRET_ID)).thenReturn("secret-id");
    when(envProvider.getEnv(HASHICORP_TOKEN)).thenReturn("token");

    Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider));

    assertThat(ex).isInstanceOf(HashicorpCredentialNotSetException.class);
    assertThat(ex).hasMessage(approleCredentialsExceptionMsg);
  }

  @Test
  public void roleIdAndSecretIdAuthEnvVarsAreSetIsAllowed() {
    when(envProvider.getEnv(HASHICORP_ROLE_ID)).thenReturn("role-id");
    when(envProvider.getEnv(HASHICORP_SECRET_ID)).thenReturn("secret-id");
    when(envProvider.getEnv(HASHICORP_TOKEN)).thenReturn(null);

    // Exception unrelated to env vars will be thrown
    Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider));

    assertThat(ex).isNotInstanceOf(HashicorpCredentialNotSetException.class);
  }

  @Test
  public void onlyTokenAuthEnvVarIsSetIsAllowed() {
    when(envProvider.getEnv(HASHICORP_ROLE_ID)).thenReturn(null);
    when(envProvider.getEnv(HASHICORP_SECRET_ID)).thenReturn(null);
    when(envProvider.getEnv(HASHICORP_TOKEN)).thenReturn("token");

    // Exception unrelated to env vars will be thrown
    Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider));

    assertThat(ex).isNotInstanceOf(HashicorpCredentialNotSetException.class);
  }

  @Test
  public void allAuthEnvVarsSetIsAllowed() {
    when(envProvider.getEnv(HASHICORP_ROLE_ID)).thenReturn("role-id");
    when(envProvider.getEnv(HASHICORP_SECRET_ID)).thenReturn("secret-id");
    when(envProvider.getEnv(HASHICORP_TOKEN)).thenReturn("token");

    // Exception unrelated to env vars will be thrown
    Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider));

    assertThat(ex).isNotInstanceOf(HashicorpCredentialNotSetException.class);
  }

  @Test
  public void exceptionThrownIfProvidedConfigHasNoKeyConfiguration() {
    when(envProvider.getEnv(HASHICORP_ROLE_ID)).thenReturn("role-id");
    when(envProvider.getEnv(HASHICORP_SECRET_ID)).thenReturn("secret-id");
    when(envProvider.getEnv(HASHICORP_TOKEN)).thenReturn(null);

    when(config.getKeys()).thenReturn(null);

    Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider));

    assertThat(ex).isInstanceOf(ConfigException.class);
    assertThat(ex)
        .hasMessageContaining(
            "Trying to create Hashicorp Vault connection but no Vault configuration provided");
  }

  @Test
  public void exceptionThrownIfProvidedConfigHasNoHashicorpKeyVaultConfig() {
    when(envProvider.getEnv(HASHICORP_ROLE_ID)).thenReturn("role-id");
    when(envProvider.getEnv(HASHICORP_SECRET_ID)).thenReturn("secret-id");
    when(envProvider.getEnv(HASHICORP_TOKEN)).thenReturn(null);

    KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    when(config.getKeys()).thenReturn(keyConfiguration);

    Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider));

    assertThat(ex).isInstanceOf(ConfigException.class);
    assertThat(ex)
        .hasMessageContaining(
            "Trying to create Hashicorp Vault connection but no Vault configuration provided");
  }

  @Test
  public void exceptionThrownIfKeyVaultConfigUrlSyntaxIncorrect() {
    when(envProvider.getEnv(HASHICORP_ROLE_ID)).thenReturn("role-id");
    when(envProvider.getEnv(HASHICORP_SECRET_ID)).thenReturn("secret-id");
    when(envProvider.getEnv(HASHICORP_TOKEN)).thenReturn("token");

    KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    when(config.getKeys()).thenReturn(keyConfiguration);

    DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);
    when(keyConfiguration.getKeyVaultConfig(KeyVaultType.HASHICORP))
        .thenReturn(Optional.of(keyVaultConfig));

    when(keyVaultConfig.getProperty("url")).thenReturn(Optional.of("noschemeurl"));
    when(keyVaultConfig.getProperty("approlePath")).thenReturn(Optional.of("approle"));

    setUpUtilMocks(keyVaultConfig);

    Throwable ex =
        catchThrowable(
            () -> keyVaultServiceFactory.create(config, envProvider, keyVaultServiceFactoryUtil));

    assertThat(ex).isExactlyInstanceOf(ConfigException.class);
    assertThat(ex.getMessage()).contains("Provided Hashicorp Vault url is incorrectly formatted");
  }

  @Test
  public void exceptionThrownIfKeyVaultConfigUrlIsMalformed() {
    when(envProvider.getEnv(HASHICORP_ROLE_ID)).thenReturn("role-id");
    when(envProvider.getEnv(HASHICORP_SECRET_ID)).thenReturn("secret-id");
    when(envProvider.getEnv(HASHICORP_TOKEN)).thenReturn("token");

    KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    when(config.getKeys()).thenReturn(keyConfiguration);

    DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);
    when(keyVaultConfig.getProperty("url")).thenReturn(Optional.of("http://malformedurl:-1"));
    when(keyVaultConfig.getProperty("approlePath")).thenReturn(Optional.of("approle"));

    when(keyConfiguration.getKeyVaultConfig(KeyVaultType.HASHICORP))
        .thenReturn(Optional.of(keyVaultConfig));

    setUpUtilMocks(keyVaultConfig);

    Throwable ex =
        catchThrowable(
            () -> keyVaultServiceFactory.create(config, envProvider, keyVaultServiceFactoryUtil));

    assertThat(ex).isExactlyInstanceOf(ConfigException.class);
    assertThat(ex.getMessage()).contains("Provided Hashicorp Vault url is incorrectly formatted");
  }

  private void setUpUtilMocks(KeyVaultConfig keyVaultConfig) {
    SslConfiguration sslConfiguration = mock(SslConfiguration.class);
    when(keyVaultServiceFactoryUtil.configureSsl(keyVaultConfig, envProvider))
        .thenReturn(sslConfiguration);

    ClientHttpRequestFactory clientHttpRequestFactory = mock(ClientHttpRequestFactory.class);
    when(keyVaultServiceFactoryUtil.createClientHttpRequestFactory(
            any(ClientOptions.class), eq(sslConfiguration)))
        .thenReturn(clientHttpRequestFactory);

    ClientAuthentication clientAuthentication = mock(ClientAuthentication.class);
    when(keyVaultServiceFactoryUtil.configureClientAuthentication(
            eq(keyVaultConfig),
            eq(envProvider),
            eq(clientHttpRequestFactory),
            any(VaultEndpoint.class)))
        .thenReturn(clientAuthentication);
  }

  @Test
  public void returnedValueIsCorrectType() {
    when(envProvider.getEnv(HASHICORP_ROLE_ID)).thenReturn("role-id");
    when(envProvider.getEnv(HASHICORP_SECRET_ID)).thenReturn("secret-id");
    when(envProvider.getEnv(HASHICORP_TOKEN)).thenReturn("token");

    KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    when(config.getKeys()).thenReturn(keyConfiguration);

    DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);
    when(keyConfiguration.getKeyVaultConfig(KeyVaultType.HASHICORP))
        .thenReturn(Optional.of(keyVaultConfig));

    when(keyVaultConfig.getProperty("url")).thenReturn(Optional.of("http://someurl"));
    when(keyVaultConfig.getProperty("approlePath")).thenReturn(Optional.of("approle"));

    setUpUtilMocks(keyVaultConfig);

    KeyVaultService result =
        keyVaultServiceFactory.create(config, envProvider, keyVaultServiceFactoryUtil);

    assertThat(result).isInstanceOf(HashicorpKeyVaultService.class);
  }

  @Test
  public void returnedValueIsCorrectTypeUsing2ArgConstructor() {
    when(envProvider.getEnv(HASHICORP_ROLE_ID)).thenReturn("role-id");
    when(envProvider.getEnv(HASHICORP_SECRET_ID)).thenReturn("secret-id");
    when(envProvider.getEnv(HASHICORP_TOKEN)).thenReturn("token");

    KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    when(config.getKeys()).thenReturn(keyConfiguration);

    DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);
    when(keyConfiguration.getKeyVaultConfig(KeyVaultType.HASHICORP))
        .thenReturn(Optional.of(keyVaultConfig));

    when(keyVaultConfig.getProperty("url")).thenReturn(Optional.of("http://someurl"));
    when(keyVaultConfig.getProperty("approlePath")).thenReturn(Optional.of("approle"));

    setUpUtilMocks(keyVaultConfig);

    KeyVaultService result = keyVaultServiceFactory.create(config, envProvider);

    assertThat(result).isInstanceOf(HashicorpKeyVaultService.class);
  }
}
