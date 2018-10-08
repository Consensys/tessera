package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.KeyVaultConfig;
import com.quorum.tessera.config.keypairs.AzureVaultKeyPair;
import com.quorum.tessera.config.keypairs.DirectKeyPair;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidatorContext;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KeyVaultConfigurationValidatorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyVaultConfigurationValidatorTest.class);

    private ConstraintValidatorContext context;
    private KeyVaultConfigurationValidator validator;

    @Before
    public void setUp() {
        context = mock(ConstraintValidatorContext.class);

        validator = new KeyVaultConfigurationValidator();
        ValidKeyVaultConfiguration validKeyVaultConfiguration = mock(ValidKeyVaultConfiguration.class);
        validator.initialize(validKeyVaultConfiguration);
    }

    @Test
    public void nullKeyConfigurationIsAllowedAndWillBePickedUpByNotNullAnnotation() {
        assertThat(validator.isValid(null, context)).isTrue();
    }

    @Test
    public void keyVaultConfigWithVaultKeyPairTypeIsValid() {
        AzureVaultKeyPair keyPair = mock(AzureVaultKeyPair.class);

        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        when(keyConfiguration.getKeyData()).thenReturn(Collections.singletonList(keyPair));
        KeyVaultConfig keyVaultConfig = mock(KeyVaultConfig.class);
        when(keyConfiguration.getAzureKeyVaultConfig()).thenReturn(keyVaultConfig);

        assertThat(validator.isValid(keyConfiguration, context)).isTrue();
    }

    @Test
    public void keyVaultConfigWithNonVaultKeyPairTypeIsValid() {
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        DirectKeyPair keyPair = mock(DirectKeyPair.class);
        KeyVaultConfig keyVaultConfig = mock(KeyVaultConfig.class);

        when(keyConfiguration.getKeyData()).thenReturn(Collections.singletonList(keyPair));
        when(keyConfiguration.getAzureKeyVaultConfig()).thenReturn(keyVaultConfig);

        assertThat(validator.isValid(keyConfiguration, context)).isTrue();
    }

    @Test
    public void noKeyVaultConfigWithVaultKeyPairTypeIsInvalid() {
        AzureVaultKeyPair keyPair = mock(AzureVaultKeyPair.class);

        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        when(keyConfiguration.getKeyData()).thenReturn(Collections.singletonList(keyPair));
        when(keyConfiguration.getAzureKeyVaultConfig()).thenReturn(null);

        assertThat(validator.isValid(keyConfiguration, context)).isFalse();
    }

    @Test
    public void noKeyVaultConfigWithNonVaultKeyPairTypeIsValid() {
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        DirectKeyPair keyPair = mock(DirectKeyPair.class);

        when(keyConfiguration.getKeyData()).thenReturn(Collections.singletonList(keyPair));
        when(keyConfiguration.getAzureKeyVaultConfig()).thenReturn(null);

        assertThat(validator.isValid(keyConfiguration, context)).isTrue();
    }

}
