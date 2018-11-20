package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.KeyVaultConfig;
import com.quorum.tessera.config.keypairs.*;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    public void keyVaultConfigWithVaultKeyPairIsValid() {
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        AzureVaultKeyPair keyPair = mock(AzureVaultKeyPair.class);
        KeyVaultConfig keyVaultConfig = mock(KeyVaultConfig.class);

        when(keyConfiguration.getKeyData()).thenReturn(Collections.singletonList(keyPair));
        when(keyConfiguration.getKeyVaultConfig()).thenReturn(keyVaultConfig);

        assertThat(validator.isValid(keyConfiguration, context)).isTrue();
    }

    @Test
    public void keyVaultConfigWithMultipleVaultKeyPairTypesIsValid() {
        List<ConfigKeyPair> keyPairs = new ArrayList<>();
        keyPairs.add(mock(AzureVaultKeyPair.class));
        keyPairs.add(mock(AzureVaultKeyPair.class));

        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        KeyVaultConfig keyVaultConfig = mock(KeyVaultConfig.class);

        when(keyConfiguration.getKeyData()).thenReturn(keyPairs);
        when(keyConfiguration.getKeyVaultConfig()).thenReturn(keyVaultConfig);

        assertThat(validator.isValid(keyConfiguration, context)).isTrue();
    }

    @Test
    public void keyVaultConfigWithMultipleKeyPairTypesIncludingVaultIsValid() {
        List<ConfigKeyPair> keyPairs = new ArrayList<>();
        keyPairs.add(mock(AzureVaultKeyPair.class));
        keyPairs.add(mock(DirectKeyPair.class));

        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        KeyVaultConfig keyVaultConfig = mock(KeyVaultConfig.class);

        when(keyConfiguration.getKeyData()).thenReturn(keyPairs);
        when(keyConfiguration.getKeyVaultConfig()).thenReturn(keyVaultConfig);

        assertThat(validator.isValid(keyConfiguration, context)).isTrue();
    }

    @Test
    public void keyVaultConfigWithNonVaultKeyPairIsValid() {
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        DirectKeyPair keyPair = mock(DirectKeyPair.class);
        KeyVaultConfig keyVaultConfig = mock(KeyVaultConfig.class);

        when(keyConfiguration.getKeyData()).thenReturn(Collections.singletonList(keyPair));
        when(keyConfiguration.getKeyVaultConfig()).thenReturn(keyVaultConfig);

        assertThat(validator.isValid(keyConfiguration, context)).isTrue();
    }

    @Test
    public void keyVaultConfigWithMultipleNonVaultKeyPairsIsValid() {
        List<ConfigKeyPair> keyPairs = new ArrayList<>();
        keyPairs.add(mock(DirectKeyPair.class));
        keyPairs.add(mock(InlineKeypair.class));

        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        KeyVaultConfig keyVaultConfig = mock(KeyVaultConfig.class);

        when(keyConfiguration.getKeyData()).thenReturn(keyPairs);
        when(keyConfiguration.getKeyVaultConfig()).thenReturn(keyVaultConfig);

        assertThat(validator.isValid(keyConfiguration, context)).isTrue();
    }

    @Test
    public void noKeyVaultConfigWithVaultKeyPairIsInvalid() {
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        AzureVaultKeyPair keyPair = mock(AzureVaultKeyPair.class);

        when(keyConfiguration.getKeyData()).thenReturn(Collections.singletonList(keyPair));
        when(keyConfiguration.getKeyVaultConfig()).thenReturn(null);

        assertThat(validator.isValid(keyConfiguration, context)).isFalse();
    }

    @Test
    public void noKeyVaultConfigWithMultipleVaultKeyPairsIsInvalid() {
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        List<ConfigKeyPair> keyPairs = new ArrayList<>();

        keyPairs.add(mock(AzureVaultKeyPair.class));
        keyPairs.add(mock(AzureVaultKeyPair.class));

        when(keyConfiguration.getKeyData()).thenReturn(keyPairs);
        when(keyConfiguration.getKeyVaultConfig()).thenReturn(null);

        assertThat(validator.isValid(keyConfiguration, context)).isFalse();
    }

    @Test
    public void noKeyVaultConfigWithMultipleKeyPairsIncludingVaultIsInvalid() {
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        List<ConfigKeyPair> keyPairs = new ArrayList<>();

        keyPairs.add(mock(AzureVaultKeyPair.class));
        keyPairs.add(mock(DirectKeyPair.class));

        when(keyConfiguration.getKeyData()).thenReturn(keyPairs);
        when(keyConfiguration.getKeyVaultConfig()).thenReturn(null);

        assertThat(validator.isValid(keyConfiguration, context)).isFalse();
    }

    @Test
    public void noKeyVaultConfigWithNonVaultKeyPairIsValid() {
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        DirectKeyPair keyPair = mock(DirectKeyPair.class);

        when(keyConfiguration.getKeyData()).thenReturn(Collections.singletonList(keyPair));
        when(keyConfiguration.getKeyVaultConfig()).thenReturn(null);

        assertThat(validator.isValid(keyConfiguration, context)).isTrue();
    }

    @Test
    public void noKeyVaultConfigWithMultipleNonVaultKeyPairsIsValid() {
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        List<ConfigKeyPair> keyPairs = new ArrayList<>();

        keyPairs.add(mock(DirectKeyPair.class));
        keyPairs.add(mock(FilesystemKeyPair.class));

        when(keyConfiguration.getKeyData()).thenReturn(keyPairs);
        when(keyConfiguration.getKeyVaultConfig()).thenReturn(null);

        assertThat(validator.isValid(keyConfiguration, context)).isTrue();
    }

}
