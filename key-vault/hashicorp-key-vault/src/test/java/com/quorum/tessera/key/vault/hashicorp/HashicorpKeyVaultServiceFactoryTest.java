package com.quorum.tessera.key.vault.hashicorp;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.api.Auth;
import com.bettercloud.vault.response.AuthResponse;
import com.quorum.tessera.config.*;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.key.vault.KeyVaultClientFactory;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class HashicorpKeyVaultServiceFactoryTest {

    private HashicorpKeyVaultServiceFactory keyVaultServiceFactory;

    private Config config;

    private EnvironmentVariableProvider envProvider;

    private HashicorpKeyVaultClientFactory keyVaultClientFactory;

    private String noCredentialsExceptionMsg = "Environment variables must be set to authenticate with Hashicorp Vault.  Set the HASHICORP_ROLE_ID and HASHICORP_SECRET_ID environment variables if using the AppRole authentication method.  Set the HASHICORP_TOKEN environment variable if using another authentication method.";

    private String approleCredentialsExceptionMsg = "Only one of the HASHICORP_ROLE_ID and HASHICORP_SECRET_ID environment variables to authenticate with Hashicorp Vault using the AppRole method has been set";

    @Before
    public void setUp() {
        this.keyVaultServiceFactory = new HashicorpKeyVaultServiceFactory();
        this.config = mock(Config.class);
        this.envProvider = mock(EnvironmentVariableProvider.class);
        this.keyVaultClientFactory = mock(HashicorpKeyVaultClientFactory.class);
    }

    @Test(expected = NullPointerException.class)
    public void nullConfigThrowsException() {
        keyVaultServiceFactory.create(null, envProvider, keyVaultClientFactory);
    }

    @Test(expected = NullPointerException.class)
    public void nullEnvVarProviderThrowsException() {
        keyVaultServiceFactory.create(config, null, keyVaultClientFactory);
    }

    @Test
    public void getType() {
        assertThat(keyVaultServiceFactory.getType()).isEqualTo(KeyVaultType.HASHICORP);
    }

    @Test
    public void exceptionThrownIfNoEnvVarsSet() {
        when(envProvider.getEnv("HASHICORP_ROLE_ID")).thenReturn(null);
        when(envProvider.getEnv("HASHICORP_SECRET_ID")).thenReturn(null);
        when(envProvider.getEnv("HASHICORP_TOKEN")).thenReturn(null);

        Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider, keyVaultClientFactory));

        assertThat(ex).isInstanceOf(HashicorpCredentialNotSetException.class);
        assertThat(ex).hasMessage(noCredentialsExceptionMsg);
    }

    @Test
    public void exceptionThrownIfOnlyRoleIdEnvVarSet() {
        when(envProvider.getEnv("HASHICORP_ROLE_ID")).thenReturn("role-id");
        when(envProvider.getEnv("HASHICORP_SECRET_ID")).thenReturn(null);
        when(envProvider.getEnv("HASHICORP_TOKEN")).thenReturn(null);

        Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider, keyVaultClientFactory));

        assertThat(ex).isInstanceOf(HashicorpCredentialNotSetException.class);
        assertThat(ex).hasMessage(approleCredentialsExceptionMsg);
    }

    @Test
    public void exceptionThrownIfOnlySecretIdEnvVarSet() {
        when(envProvider.getEnv("HASHICORP_ROLE_ID")).thenReturn(null);
        when(envProvider.getEnv("HASHICORP_SECRET_ID")).thenReturn("secret-id");
        when(envProvider.getEnv("HASHICORP_TOKEN")).thenReturn(null);

        Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider, keyVaultClientFactory));

        assertThat(ex).isInstanceOf(HashicorpCredentialNotSetException.class);
        assertThat(ex).hasMessage(approleCredentialsExceptionMsg);
    }

    @Test
    public void exceptionThrownIfOnlyRoleIdAndTokenEnvVarsSet() {
        when(envProvider.getEnv("HASHICORP_ROLE_ID")).thenReturn("role-id");
        when(envProvider.getEnv("HASHICORP_SECRET_ID")).thenReturn(null);
        when(envProvider.getEnv("HASHICORP_TOKEN")).thenReturn("token");

        Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider, keyVaultClientFactory));

        assertThat(ex).isInstanceOf(HashicorpCredentialNotSetException.class);
        assertThat(ex).hasMessage(approleCredentialsExceptionMsg);
    }

    @Test
    public void exceptionThrownIfOnlySecretIdAndTokenEnvVarsSet() {
        when(envProvider.getEnv("HASHICORP_ROLE_ID")).thenReturn(null);
        when(envProvider.getEnv("HASHICORP_SECRET_ID")).thenReturn("secret-id");
        when(envProvider.getEnv("HASHICORP_TOKEN")).thenReturn("token");

        Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider, keyVaultClientFactory));

        assertThat(ex).isInstanceOf(HashicorpCredentialNotSetException.class);
        assertThat(ex).hasMessage(approleCredentialsExceptionMsg);
    }

    @Test
    public void roleIdAndSecretIdEnvVarsAreSetIsAllowed() {
        when(envProvider.getEnv("HASHICORP_ROLE_ID")).thenReturn("role-id");
        when(envProvider.getEnv("HASHICORP_SECRET_ID")).thenReturn("secret-id");
        when(envProvider.getEnv("HASHICORP_TOKEN")).thenReturn(null);

        //Exception unrelated to env vars will be thrown
        Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider, keyVaultClientFactory));

        assertThat(ex).isNotInstanceOf(HashicorpCredentialNotSetException.class);
    }

    @Test
    public void onlyTokenEnvVarIsSetIsAllowed() {
        when(envProvider.getEnv("HASHICORP_ROLE_ID")).thenReturn(null);
        when(envProvider.getEnv("HASHICORP_SECRET_ID")).thenReturn(null);
        when(envProvider.getEnv("HASHICORP_TOKEN")).thenReturn("token");

        //Exception unrelated to env vars will be thrown
        Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider, keyVaultClientFactory));

        assertThat(ex).isNotInstanceOf(HashicorpCredentialNotSetException.class);
    }

    @Test
    public void allEnvVarsSetIsAllowed() {
        when(envProvider.getEnv("HASHICORP_ROLE_ID")).thenReturn("role-id");
        when(envProvider.getEnv("HASHICORP_SECRET_ID")).thenReturn("secret-id");
        when(envProvider.getEnv("HASHICORP_TOKEN")).thenReturn("token");

        //Exception unrelated to env vars will be thrown
        Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider, keyVaultClientFactory));

        assertThat(ex).isNotInstanceOf(HashicorpCredentialNotSetException.class);
    }

    @Test
    public void exceptionThrownIfProvidedConfigHasNoKeyConfiguration() {
        when(envProvider.getEnv("HASHICORP_ROLE_ID")).thenReturn("role-id");
        when(envProvider.getEnv("HASHICORP_SECRET_ID")).thenReturn("secret-id");
        when(envProvider.getEnv("HASHICORP_TOKEN")).thenReturn(null);

        when(config.getKeys()).thenReturn(null);

        Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider, keyVaultClientFactory));

        assertThat(ex).isInstanceOf(ConfigException.class);
        assertThat(ex).hasMessageContaining("Trying to create Hashicorp Vault connection but no Vault configuration provided");
    }

    @Test
    public void exceptionThrownIfProvidedConfigHasNoHashicorpKeyVaultConfig() {
        when(envProvider.getEnv("HASHICORP_ROLE_ID")).thenReturn("role-id");
        when(envProvider.getEnv("HASHICORP_SECRET_ID")).thenReturn("secret-id");
        when(envProvider.getEnv("HASHICORP_TOKEN")).thenReturn(null);

        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        when(config.getKeys()).thenReturn(keyConfiguration);

        when(keyConfiguration.getHashicorpKeyVaultConfig()).thenReturn(null);

        Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider, keyVaultClientFactory));

        assertThat(ex).isInstanceOf(ConfigException.class);
        assertThat(ex).hasMessageContaining("Trying to create Hashicorp Vault connection but no Vault configuration provided");
    }

    @Test
    public void exceptionThrownIfKeyVaultClientFactoryNotHashicorpImplementation() {
        when(envProvider.getEnv("HASHICORP_ROLE_ID")).thenReturn("role-id");
        when(envProvider.getEnv("HASHICORP_SECRET_ID")).thenReturn("secret-id");
        when(envProvider.getEnv("HASHICORP_TOKEN")).thenReturn(null);

        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        when(config.getKeys()).thenReturn(keyConfiguration);
        when(keyConfiguration.getHashicorpKeyVaultConfig()).thenReturn(mock(HashicorpKeyVaultConfig.class));

        KeyVaultClientFactory wrongImpl = mock(KeyVaultClientFactory.class);

        Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider, wrongImpl));

        assertThat(ex).isInstanceOf(HashicorpVaultException.class);
        assertThat(ex).hasMessageContaining("Incorrect KeyVaultClientFactoryType passed to HashicorpKeyVaultServiceFactory");
    }

    @Test
    public void ifRoleIdAndSecretIdEnvVarsSetThenAppRoleIsUsedToAuthenticate() throws Exception {
        when(envProvider.getEnv("HASHICORP_ROLE_ID")).thenReturn("role-id");
        when(envProvider.getEnv("HASHICORP_SECRET_ID")).thenReturn("secret-id");
        when(envProvider.getEnv("HASHICORP_TOKEN")).thenReturn(null);

        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        when(config.getKeys()).thenReturn(keyConfiguration);

        HashicorpKeyVaultConfig keyVaultConfig = mock(HashicorpKeyVaultConfig.class);
        when(keyConfiguration.getHashicorpKeyVaultConfig()).thenReturn(keyVaultConfig);
        when(keyVaultConfig.getApprolePath()).thenReturn("approle");

        Vault unauthenticatedVault = mock(Vault.class);
        when(
            keyVaultClientFactory
                .createUnauthenticatedClient(any(HashicorpKeyVaultConfig.class), any(VaultConfigFactory.class), any(SslConfigFactory.class))
        ).thenReturn(unauthenticatedVault);

        Auth auth = mock(Auth.class);
        when(unauthenticatedVault.auth()).thenReturn(auth);
        AuthResponse loginResponse = mock(AuthResponse.class);
        when(auth.loginByAppRole(anyString(), anyString(), anyString())).thenReturn(loginResponse);
        String token = "token";
        when(loginResponse.getAuthClientToken()).thenReturn(token);

        keyVaultServiceFactory.create(config, envProvider, keyVaultClientFactory);

        verify(auth).loginByAppRole("approle", "role-id", "secret-id");
        verify(keyVaultClientFactory).createAuthenticatedClient(any(HashicorpKeyVaultConfig.class), any(VaultConfigFactory.class), any(SslConfigFactory.class), matches(token));
    }

    @Test
    public void ifAllEnvVarsSetThenAppRoleIsUsedToAuthenticate() throws Exception {
        when(envProvider.getEnv("HASHICORP_ROLE_ID")).thenReturn("role-id");
        when(envProvider.getEnv("HASHICORP_SECRET_ID")).thenReturn("secret-id");
        when(envProvider.getEnv("HASHICORP_TOKEN")).thenReturn("env-token");

        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        when(config.getKeys()).thenReturn(keyConfiguration);

        HashicorpKeyVaultConfig keyVaultConfig = mock(HashicorpKeyVaultConfig.class);
        when(keyConfiguration.getHashicorpKeyVaultConfig()).thenReturn(keyVaultConfig);
        when(keyVaultConfig.getApprolePath()).thenReturn("approle");

        Vault unauthenticatedVault = mock(Vault.class);
        when(
            keyVaultClientFactory
                .createUnauthenticatedClient(any(HashicorpKeyVaultConfig.class), any(VaultConfigFactory.class), any(SslConfigFactory.class))
        ).thenReturn(unauthenticatedVault);

        Auth auth = mock(Auth.class);
        when(unauthenticatedVault.auth()).thenReturn(auth);
        AuthResponse loginResponse = mock(AuthResponse.class);
        when(auth.loginByAppRole(anyString(), anyString(), anyString())).thenReturn(loginResponse);
        String token = "token";
        when(loginResponse.getAuthClientToken()).thenReturn(token);

        keyVaultServiceFactory.create(config, envProvider, keyVaultClientFactory);

        verify(auth).loginByAppRole("approle", "role-id", "secret-id");
        verify(keyVaultClientFactory).createAuthenticatedClient(any(HashicorpKeyVaultConfig.class), any(VaultConfigFactory.class), any(SslConfigFactory.class), matches(token));
    }

    @Test
    public void exceptionThrownIfErrorEncounteredDuringAppRoleAuthentication() throws Exception {
        when(envProvider.getEnv("HASHICORP_ROLE_ID")).thenReturn("role-id");
        when(envProvider.getEnv("HASHICORP_SECRET_ID")).thenReturn("secret-id");
        when(envProvider.getEnv("HASHICORP_TOKEN")).thenReturn(null);

        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        when(config.getKeys()).thenReturn(keyConfiguration);

        HashicorpKeyVaultConfig keyVaultConfig = mock(HashicorpKeyVaultConfig.class);
        when(keyConfiguration.getHashicorpKeyVaultConfig()).thenReturn(keyVaultConfig);
        when(keyVaultConfig.getApprolePath()).thenReturn("approle");

        Vault unauthenticatedVault = mock(Vault.class);
        when(
            keyVaultClientFactory
                .createUnauthenticatedClient(any(HashicorpKeyVaultConfig.class), any(VaultConfigFactory.class), any(SslConfigFactory.class))
        ).thenReturn(unauthenticatedVault);

        Auth auth = mock(Auth.class);
        when(unauthenticatedVault.auth()).thenReturn(auth);
        AuthResponse loginResponse = mock(AuthResponse.class);
        when(auth.loginByAppRole(anyString(), anyString(), anyString())).thenThrow(VaultException.class);

        Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider, keyVaultClientFactory));

        assertThat(ex).isInstanceOf(HashicorpVaultException.class);
        assertThat(ex.getMessage()).contains("Unable to authenticate using AppRole");
    }

    @Test
    public void ifOnlyTokenEnvVarSetThenTokenIsUsedToAuthenticate() throws Exception {
        when(envProvider.getEnv("HASHICORP_ROLE_ID")).thenReturn(null);
        when(envProvider.getEnv("HASHICORP_SECRET_ID")).thenReturn(null);
        when(envProvider.getEnv("HASHICORP_TOKEN")).thenReturn("env-token");

        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        when(config.getKeys()).thenReturn(keyConfiguration);

        HashicorpKeyVaultConfig keyVaultConfig = mock(HashicorpKeyVaultConfig.class);
        when(keyConfiguration.getHashicorpKeyVaultConfig()).thenReturn(keyVaultConfig);

        Vault unauthenticatedVault = mock(Vault.class);
        when(
            keyVaultClientFactory
                .createUnauthenticatedClient(any(HashicorpKeyVaultConfig.class), any(VaultConfigFactory.class), any(SslConfigFactory.class))
        ).thenReturn(unauthenticatedVault);

        keyVaultServiceFactory.create(config, envProvider, keyVaultClientFactory);

        verify(keyVaultClientFactory).createAuthenticatedClient(any(HashicorpKeyVaultConfig.class), any(VaultConfigFactory.class), any(SslConfigFactory.class), matches("env-token"));
    }

}
