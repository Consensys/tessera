package com.quorum.tessera.key.vault.aws;

import com.quorum.tessera.config.AWSKeyVaultConfig;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigException;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.KeyVaultType;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.key.vault.KeyVaultService;
import org.junit.Before;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AWSKeyVaultServiceFactoryTest {

    private AWSKeyVaultServiceFactory awsKeyVaultServiceFactory;

    private Config config;

    private EnvironmentVariableProvider envProvider;

    @Before
    public void setUp() {
        this.config = mock(Config.class);
        this.envProvider = mock(EnvironmentVariableProvider.class);
        this.awsKeyVaultServiceFactory = new AWSKeyVaultServiceFactory();
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
        when(keyConfiguration.getAzureKeyVaultConfig()).thenReturn(null);
        when(config.getKeys()).thenReturn(keyConfiguration);

        Throwable ex = catchThrowable(() -> awsKeyVaultServiceFactory.create(config, envProvider));

        assertThat(ex).isExactlyInstanceOf(ConfigException.class);
        assertThat(ex.getMessage())
                .contains("Trying to create AWS Secrets Manager connection but no configuration provided");
    }

    @Test
    public void envVarsAndKeyVaultConfigProvidedCreatesAWSKeyVaultService() {
        when(envProvider.getEnv(anyString())).thenReturn("envVar");
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        AWSKeyVaultConfig keyVaultConfig = mock(AWSKeyVaultConfig.class);
        when(keyConfiguration.getAwsKeyVaultConfig()).thenReturn(keyVaultConfig);
        when(config.getKeys()).thenReturn(keyConfiguration);

        KeyVaultService result = awsKeyVaultServiceFactory.create(config, envProvider);

        assertThat(result).isInstanceOf(AWSKeyVaultService.class);
    }

    @Test
    public void getType() {
        assertThat(awsKeyVaultServiceFactory.getType()).isEqualTo(KeyVaultType.AWS);
    }
}
