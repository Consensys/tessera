package com.quorum.tessera.key.vault.hashicorp;

import static com.quorum.tessera.config.util.EnvironmentVariables.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.quorum.tessera.config.KeyVaultConfig;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.vault.authentication.AppRoleAuthentication;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.support.ClientOptions;
import org.springframework.vault.support.SslConfiguration;

public class HashicorpKeyVaultServiceFactoryUtilTest {

  private HashicorpKeyVaultServiceFactoryUtil util;

  @Before
  public void setUp() {
    this.util = new HashicorpKeyVaultServiceFactoryUtil();
  }

  @Test
  public void configureSslUsesKeyStoreAndTrustStoreIfBothProvided() throws Exception {
    KeyVaultConfig keyVaultConfig = mock(KeyVaultConfig.class);
    EnvironmentVariableProvider envProvider = mock(EnvironmentVariableProvider.class);

    Path path = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
    path.toFile().deleteOnExit();

    when(keyVaultConfig.hasProperty("tlsKeyStorePath", "tlsTrustStorePath")).thenReturn(true);

    when(keyVaultConfig.getProperty("tlsKeyStorePath")).thenReturn(Optional.of(path.toString()));
    when(keyVaultConfig.getProperty("tlsTrustStorePath")).thenReturn(Optional.of(path.toString()));

    SslConfiguration result = util.configureSsl(keyVaultConfig, envProvider);

    assertThat(result.getKeyStoreConfiguration().isPresent()).isTrue();
    assertThat(result.getTrustStoreConfiguration().isPresent()).isTrue();
  }

  @Test
  public void configureSslUsesTrustStoreOnlyIfProvided() throws Exception {
    KeyVaultConfig keyVaultConfig = mock(KeyVaultConfig.class);
    EnvironmentVariableProvider envProvider = mock(EnvironmentVariableProvider.class);

    Path path = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
    path.toFile().deleteOnExit();

    when(keyVaultConfig.hasProperty("tlsTrustStorePath")).thenReturn(true);
    when(keyVaultConfig.hasProperty("tlsKeyStorePath")).thenReturn(false);

    when(keyVaultConfig.getProperty("tlsKeyStorePath")).thenReturn(Optional.empty());
    when(keyVaultConfig.getProperty("tlsTrustStorePath")).thenReturn(Optional.of(path.toString()));

    SslConfiguration result = util.configureSsl(keyVaultConfig, envProvider);

    assertThat(result.getKeyStoreConfiguration().isPresent()).isFalse();
    assertThat(result.getTrustStoreConfiguration().isPresent()).isTrue();
  }

  @Test
  public void configureSslUsesNoKeyStoresIfNoneProvided() {
    KeyVaultConfig keyVaultConfig = mock(KeyVaultConfig.class);
    EnvironmentVariableProvider envProvider = mock(EnvironmentVariableProvider.class);

    when(keyVaultConfig.getProperty("tlsKeyStorePath")).thenReturn(Optional.empty());
    when(keyVaultConfig.getProperty("tlsTrustStorePath")).thenReturn(Optional.empty());

    SslConfiguration result = util.configureSsl(keyVaultConfig, envProvider);

    assertThat(result.getKeyStoreConfiguration().isPresent()).isFalse();
    assertThat(result.getTrustStoreConfiguration().isPresent()).isFalse();
  }

  @Test
  public void createClientHttpRequestFactory() {
    ClientOptions clientOptions = mock(ClientOptions.class);
    SslConfiguration sslConfiguration = mock(SslConfiguration.class);

    SslConfiguration.KeyStoreConfiguration keyStoreConfiguration =
        mock(SslConfiguration.KeyStoreConfiguration.class);
    when(sslConfiguration.getKeyStoreConfiguration()).thenReturn(keyStoreConfiguration);
    when(sslConfiguration.getTrustStoreConfiguration()).thenReturn(keyStoreConfiguration);

    when(clientOptions.getConnectionTimeout()).thenReturn(Duration.ZERO);
    when(clientOptions.getReadTimeout()).thenReturn(Duration.ZERO);

    ClientHttpRequestFactory result =
        util.createClientHttpRequestFactory(clientOptions, sslConfiguration);

    assertThat(result).isInstanceOf(OkHttp3ClientHttpRequestFactory.class);
  }

  @Test
  public void configureClientAuthenticationIfAllEnvVarsSetThenAppRoleMethod() {
    KeyVaultConfig keyVaultConfig = mock(KeyVaultConfig.class);
    EnvironmentVariableProvider envProvider = mock(EnvironmentVariableProvider.class);
    ClientHttpRequestFactory clientHttpRequestFactory = mock(ClientHttpRequestFactory.class);
    VaultEndpoint vaultEndpoint = mock(VaultEndpoint.class);

    when(envProvider.getEnv(HASHICORP_ROLE_ID)).thenReturn("role-id");
    when(envProvider.getEnv(HASHICORP_SECRET_ID)).thenReturn("secret-id");
    when(envProvider.getEnv(HASHICORP_TOKEN)).thenReturn("token");

    when(keyVaultConfig.getProperty("approlePath")).thenReturn(Optional.of("approle"));

    ClientAuthentication result =
        util.configureClientAuthentication(
            keyVaultConfig, envProvider, clientHttpRequestFactory, vaultEndpoint);

    assertThat(result).isInstanceOf(AppRoleAuthentication.class);
  }

  @Test
  public void configureClientAuthenticationIfOnlyRoleIdAndSecretIdSetThenAppRoleMethod() {
    KeyVaultConfig keyVaultConfig = mock(KeyVaultConfig.class);
    EnvironmentVariableProvider envProvider = mock(EnvironmentVariableProvider.class);
    ClientHttpRequestFactory clientHttpRequestFactory = mock(ClientHttpRequestFactory.class);
    VaultEndpoint vaultEndpoint = mock(VaultEndpoint.class);

    when(envProvider.getEnv(HASHICORP_ROLE_ID)).thenReturn("role-id");
    when(envProvider.getEnv(HASHICORP_SECRET_ID)).thenReturn("secret-id");
    when(envProvider.getEnv(HASHICORP_TOKEN)).thenReturn(null);

    when(keyVaultConfig.getProperty("approlePath")).thenReturn(Optional.of("somepath"));

    ClientAuthentication result =
        util.configureClientAuthentication(
            keyVaultConfig, envProvider, clientHttpRequestFactory, vaultEndpoint);

    assertThat(result).isInstanceOf(AppRoleAuthentication.class);
  }

  @Test
  public void configureClientAuthenticationIfOnlyRoleIdSetThenException() {
    KeyVaultConfig keyVaultConfig = mock(KeyVaultConfig.class);
    EnvironmentVariableProvider envProvider = mock(EnvironmentVariableProvider.class);
    ClientHttpRequestFactory clientHttpRequestFactory = mock(ClientHttpRequestFactory.class);
    VaultEndpoint vaultEndpoint = mock(VaultEndpoint.class);

    when(envProvider.getEnv(HASHICORP_ROLE_ID)).thenReturn("role-id");
    when(envProvider.getEnv(HASHICORP_SECRET_ID)).thenReturn(null);
    when(envProvider.getEnv(HASHICORP_TOKEN)).thenReturn(null);

    Throwable ex =
        catchThrowable(
            () ->
                util.configureClientAuthentication(
                    keyVaultConfig, envProvider, clientHttpRequestFactory, vaultEndpoint));

    assertThat(ex).isExactlyInstanceOf(HashicorpCredentialNotSetException.class);
    assertThat(ex.getMessage())
        .isEqualTo(
            "Both "
                + HASHICORP_ROLE_ID
                + " and "
                + HASHICORP_SECRET_ID
                + " environment variables must be set to use the AppRole authentication method");
  }

  @Test
  public void configureClientAuthenticationIfOnlySecretIdSetThenException() {
    KeyVaultConfig keyVaultConfig = mock(KeyVaultConfig.class);
    EnvironmentVariableProvider envProvider = mock(EnvironmentVariableProvider.class);
    ClientHttpRequestFactory clientHttpRequestFactory = mock(ClientHttpRequestFactory.class);
    VaultEndpoint vaultEndpoint = mock(VaultEndpoint.class);

    when(envProvider.getEnv(HASHICORP_ROLE_ID)).thenReturn(null);
    when(envProvider.getEnv(HASHICORP_SECRET_ID)).thenReturn("secret-id");
    when(envProvider.getEnv(HASHICORP_TOKEN)).thenReturn(null);

    Throwable ex =
        catchThrowable(
            () ->
                util.configureClientAuthentication(
                    keyVaultConfig, envProvider, clientHttpRequestFactory, vaultEndpoint));

    assertThat(ex).isExactlyInstanceOf(HashicorpCredentialNotSetException.class);
    assertThat(ex.getMessage())
        .isEqualTo(
            "Both "
                + HASHICORP_ROLE_ID
                + " and "
                + HASHICORP_SECRET_ID
                + " environment variables must be set to use the AppRole authentication method");
  }

  @Test
  public void configureClientAuthenticationIfOnlyTokenSetThenTokenMethod() {
    KeyVaultConfig keyVaultConfig = mock(KeyVaultConfig.class);
    EnvironmentVariableProvider envProvider = mock(EnvironmentVariableProvider.class);
    ClientHttpRequestFactory clientHttpRequestFactory = mock(ClientHttpRequestFactory.class);
    VaultEndpoint vaultEndpoint = mock(VaultEndpoint.class);

    when(envProvider.getEnv(HASHICORP_ROLE_ID)).thenReturn(null);
    when(envProvider.getEnv(HASHICORP_SECRET_ID)).thenReturn(null);
    when(envProvider.getEnv(HASHICORP_TOKEN)).thenReturn("token");

    ClientAuthentication result =
        util.configureClientAuthentication(
            keyVaultConfig, envProvider, clientHttpRequestFactory, vaultEndpoint);

    assertThat(result).isInstanceOf(TokenAuthentication.class);
  }

  @Test
  public void configureClientAuthenticationIfNoEnvVarSetThenException() {
    KeyVaultConfig keyVaultConfig = mock(KeyVaultConfig.class);
    EnvironmentVariableProvider envProvider = mock(EnvironmentVariableProvider.class);
    ClientHttpRequestFactory clientHttpRequestFactory = mock(ClientHttpRequestFactory.class);
    VaultEndpoint vaultEndpoint = mock(VaultEndpoint.class);

    when(envProvider.getEnv(HASHICORP_ROLE_ID)).thenReturn(null);
    when(envProvider.getEnv(HASHICORP_SECRET_ID)).thenReturn(null);
    when(envProvider.getEnv(HASHICORP_TOKEN)).thenReturn(null);

    Throwable ex =
        catchThrowable(
            () ->
                util.configureClientAuthentication(
                    keyVaultConfig, envProvider, clientHttpRequestFactory, vaultEndpoint));

    assertThat(ex).isExactlyInstanceOf(HashicorpCredentialNotSetException.class);
    assertThat(ex.getMessage())
        .isEqualTo(
            "Both "
                + HASHICORP_ROLE_ID
                + " and "
                + HASHICORP_SECRET_ID
                + " environment variables must be set to use the AppRole authentication method.  Alternatively set "
                + HASHICORP_TOKEN
                + " to authenticate using the Token method");
  }
}
