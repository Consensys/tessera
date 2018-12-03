package com.quorum.tessera.key.vault.hashicorp;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigException;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.KeyVaultType;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HashicorpKeyVaultServiceFactoryTest {

    private HashicorpKeyVaultServiceFactory keyVaultServiceFactory;

    private Config config;

    private EnvironmentVariableProvider envProvider;

    private String noCredentialsExceptionMsg = "Environment variables must be set to authenticate with Hashicorp Vault.  Set the HASHICORP_ROLE_ID and HASHICORP_SECRET_ID environment variables if using the AppRole authentication method.  Set the HASHICORP_TOKEN environment variable if using another authentication method.";

    private String approleCredentialsExceptionMsg = "Only one of the HASHICORP_ROLE_ID and HASHICORP_SECRET_ID environment variables to authenticate with Hashicorp Vault using the AppRole method has been set";

    @Before
    public void setUp() {
        this.keyVaultServiceFactory = new HashicorpKeyVaultServiceFactory();
        this.config = mock(Config.class);
        this.envProvider = mock(EnvironmentVariableProvider.class);
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
    public void exceptionThrownIfNoEnvVarsSet() {
        when(envProvider.getEnv("HASHICORP_ROLE_ID")).thenReturn(null);
        when(envProvider.getEnv("HASHICORP_SECRET_ID")).thenReturn(null);
        when(envProvider.getEnv("HASHICORP_TOKEN")).thenReturn(null);

        Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider));

        assertThat(ex).isInstanceOf(HashicorpCredentialNotSetException.class);
        assertThat(ex).hasMessage(noCredentialsExceptionMsg);
    }

    @Test
    public void exceptionThrownIfOnlyRoleIdEnvVarSet() {
        when(envProvider.getEnv("HASHICORP_ROLE_ID")).thenReturn("role-id");
        when(envProvider.getEnv("HASHICORP_SECRET_ID")).thenReturn(null);
        when(envProvider.getEnv("HASHICORP_TOKEN")).thenReturn(null);

        Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider));

        assertThat(ex).isInstanceOf(HashicorpCredentialNotSetException.class);
        assertThat(ex).hasMessage(approleCredentialsExceptionMsg);
    }

    @Test
    public void exceptionThrownIfOnlySecretIdEnvVarSet() {
        when(envProvider.getEnv("HASHICORP_ROLE_ID")).thenReturn(null);
        when(envProvider.getEnv("HASHICORP_SECRET_ID")).thenReturn("secret-id");
        when(envProvider.getEnv("HASHICORP_TOKEN")).thenReturn(null);

        Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider));

        assertThat(ex).isInstanceOf(HashicorpCredentialNotSetException.class);
        assertThat(ex).hasMessage(approleCredentialsExceptionMsg);
    }

    @Test
    public void exceptionThrownIfOnlyRoleIdAndTokenEnvVarsSet() {
        when(envProvider.getEnv("HASHICORP_ROLE_ID")).thenReturn("role-id");
        when(envProvider.getEnv("HASHICORP_SECRET_ID")).thenReturn(null);
        when(envProvider.getEnv("HASHICORP_TOKEN")).thenReturn("token");

        Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider));

        assertThat(ex).isInstanceOf(HashicorpCredentialNotSetException.class);
        assertThat(ex).hasMessage(approleCredentialsExceptionMsg);
    }

    @Test
    public void exceptionThrownIfOnlySecretIdAndTokenEnvVarsSet() {
        when(envProvider.getEnv("HASHICORP_ROLE_ID")).thenReturn(null);
        when(envProvider.getEnv("HASHICORP_SECRET_ID")).thenReturn("secret-id");
        when(envProvider.getEnv("HASHICORP_TOKEN")).thenReturn("token");

        Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider));

        assertThat(ex).isInstanceOf(HashicorpCredentialNotSetException.class);
        assertThat(ex).hasMessage(approleCredentialsExceptionMsg);
    }

    @Test
    public void roleIdAndSecretIdEnvVarsAreSetIsAllowed() {
        when(envProvider.getEnv("HASHICORP_ROLE_ID")).thenReturn("role-id");
        when(envProvider.getEnv("HASHICORP_SECRET_ID")).thenReturn("secret-id");
        when(envProvider.getEnv("HASHICORP_TOKEN")).thenReturn(null);

        //Exception unrelated to env vars will be thrown
        Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider));

        assertThat(ex).isNotInstanceOf(HashicorpCredentialNotSetException.class);
    }

    @Test
    public void onlyTokenEnvVarIsSetIsAllowed() {
        when(envProvider.getEnv("HASHICORP_ROLE_ID")).thenReturn(null);
        when(envProvider.getEnv("HASHICORP_SECRET_ID")).thenReturn(null);
        when(envProvider.getEnv("HASHICORP_TOKEN")).thenReturn("token");

        //Exception unrelated to env vars will be thrown
        Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider));

        assertThat(ex).isNotInstanceOf(HashicorpCredentialNotSetException.class);
    }

    @Test
    public void allEnvVarsSetIsAllowed() {
        when(envProvider.getEnv("HASHICORP_ROLE_ID")).thenReturn("role-id");
        when(envProvider.getEnv("HASHICORP_SECRET_ID")).thenReturn("secret-id");
        when(envProvider.getEnv("HASHICORP_TOKEN")).thenReturn("token");

        //Exception unrelated to env vars will be thrown
        Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider));

        assertThat(ex).isNotInstanceOf(HashicorpCredentialNotSetException.class);
    }

    @Test
    public void exceptionThrownIfProvidedConfigHasNoKeyConfiguration() {
        when(envProvider.getEnv("HASHICORP_ROLE_ID")).thenReturn("role-id");
        when(envProvider.getEnv("HASHICORP_SECRET_ID")).thenReturn("secret-id");
        when(envProvider.getEnv("HASHICORP_TOKEN")).thenReturn(null);

        when(config.getKeys()).thenReturn(null);

        Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider));

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

        Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider));

        assertThat(ex).isInstanceOf(ConfigException.class);
        assertThat(ex).hasMessageContaining("Trying to create Hashicorp Vault connection but no Vault configuration provided");
    }

    @Test
    public void tlsConfigAddedIfProvided() {

    }

}

//nullConfigThrowsException
//nullEnvVarProviderThrowsException
//createThrowsExceptionIfTokenEnvVarNotSet
//nullKeyConfigurationThrowsException
//nullHashicorpVaultConfigThrowsException
//incorrectSyntaxUrlInConfigThrowsException
//incorrectlyFormattedUrlInConfigThrowsException
//createReturnsNewHashicorpKeyVaultService
//getType
