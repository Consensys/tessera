package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keypairs.*;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintValidatorContext;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MatchingKeyVaultConfigsForKeyDataValidatorTest {

    private ConstraintValidatorContext context;

    private MatchingKeyVaultConfigsForKeyDataValidator validator;

    @Before
    public void setUp() {
        context = mock(ConstraintValidatorContext.class);

        ConstraintValidatorContext.ConstraintViolationBuilder builder =
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(any(String.class))).thenReturn(builder);

        validator = new MatchingKeyVaultConfigsForKeyDataValidator();
    }

    @Test
    public void nullKeyConfigurationIsAllowedAndWillBePickedUpByNotNullAnnotation() {
        assertThat(validator.isValid(null, context)).isTrue();
    }

    @Test
    public void azureConfigWithAzureKeyPairIsValid() {
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        AzureVaultKeyPair keyPair = mock(AzureVaultKeyPair.class);
        DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);

        when(keyConfiguration.getKeyData()).thenReturn(Collections.singletonList(keyPair));
        when(keyConfiguration.getKeyVaultConfig(KeyVaultType.AZURE)).thenReturn(Optional.of(keyVaultConfig));

        assertThat(validator.isValid(keyConfiguration, context)).isTrue();
    }

    @Test
    public void azureConfigWithMultipleAzureKeyPairsIsValid() {
        List<ConfigKeyPair> keyPairs = new ArrayList<>();
        keyPairs.add(mock(AzureVaultKeyPair.class));
        keyPairs.add(mock(AzureVaultKeyPair.class));

        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);

        when(keyConfiguration.getKeyData()).thenReturn(keyPairs);
        when(keyConfiguration.getKeyVaultConfig(KeyVaultType.AZURE)).thenReturn(Optional.of(keyVaultConfig));

        assertThat(validator.isValid(keyConfiguration, context)).isTrue();
    }

    @Test
    public void azureConfigWithMultipleKeyPairTypesIncludingAzureIsValid() {
        List<ConfigKeyPair> keyPairs = new ArrayList<>();
        keyPairs.add(mock(AzureVaultKeyPair.class));
        keyPairs.add(mock(DirectKeyPair.class));

        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);

        when(keyConfiguration.getKeyData()).thenReturn(keyPairs);
        when(keyConfiguration.getKeyVaultConfig(KeyVaultType.AZURE)).thenReturn(Optional.of(keyVaultConfig));

        assertThat(validator.isValid(keyConfiguration, context)).isTrue();
    }

    @Test
    public void azureConfigWithNonAzureKeyPairIsValid() {
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        DirectKeyPair keyPair = mock(DirectKeyPair.class);
        AzureKeyVaultConfig keyVaultConfig = mock(AzureKeyVaultConfig.class);

        when(keyConfiguration.getKeyData()).thenReturn(Collections.singletonList(keyPair));
        when(keyConfiguration.getAzureKeyVaultConfig()).thenReturn(keyVaultConfig);

        assertThat(validator.isValid(keyConfiguration, context)).isTrue();
    }

    @Test
    public void azureConfigWithMultipleNonAzureKeyPairsIsValid() {
        List<ConfigKeyPair> keyPairs = new ArrayList<>();
        keyPairs.add(mock(DirectKeyPair.class));
        keyPairs.add(mock(InlineKeypair.class));

        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        AzureKeyVaultConfig keyVaultConfig = mock(AzureKeyVaultConfig.class);

        when(keyConfiguration.getKeyData()).thenReturn(keyPairs);
        when(keyConfiguration.getAzureKeyVaultConfig()).thenReturn(keyVaultConfig);

        assertThat(validator.isValid(keyConfiguration, context)).isTrue();
    }

    @Test
    public void noAzureConfigWithAzureKeyPairIsInvalid() {
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        AzureVaultKeyPair keyPair = mock(AzureVaultKeyPair.class);

        when(keyConfiguration.getKeyData()).thenReturn(Collections.singletonList(keyPair));
        when(keyConfiguration.getAzureKeyVaultConfig()).thenReturn(null);

        assertThat(validator.isValid(keyConfiguration, context)).isFalse();
        verify(context).buildConstraintViolationWithTemplate("{MatchingKeyVaultConfigsForKeyData.azure.message}");
    }

    @Test
    public void noAzureConfigWithMultipleAzureKeyPairsIsInvalid() {
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        List<ConfigKeyPair> keyPairs = new ArrayList<>();

        keyPairs.add(mock(AzureVaultKeyPair.class));
        keyPairs.add(mock(AzureVaultKeyPair.class));

        when(keyConfiguration.getKeyData()).thenReturn(keyPairs);
        when(keyConfiguration.getAzureKeyVaultConfig()).thenReturn(null);

        assertThat(validator.isValid(keyConfiguration, context)).isFalse();
        verify(context).buildConstraintViolationWithTemplate("{MatchingKeyVaultConfigsForKeyData.azure.message}");
    }

    @Test
    public void noAzureConfigWithMultipleKeyPairsIncludingAzureIsInvalid() {
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        List<ConfigKeyPair> keyPairs = new ArrayList<>();

        keyPairs.add(mock(AzureVaultKeyPair.class));
        keyPairs.add(mock(DirectKeyPair.class));

        when(keyConfiguration.getKeyData()).thenReturn(keyPairs);
        when(keyConfiguration.getAzureKeyVaultConfig()).thenReturn(null);

        assertThat(validator.isValid(keyConfiguration, context)).isFalse();
        verify(context).buildConstraintViolationWithTemplate("{MatchingKeyVaultConfigsForKeyData.azure.message}");
    }

    @Test
    public void noAzureConfigWithNonAzureKeyPairIsValid() {
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        DirectKeyPair keyPair = mock(DirectKeyPair.class);

        when(keyConfiguration.getKeyData()).thenReturn(Collections.singletonList(keyPair));
        when(keyConfiguration.getAzureKeyVaultConfig()).thenReturn(null);

        assertThat(validator.isValid(keyConfiguration, context)).isTrue();
    }

    @Test
    public void noAzureConfigWithMultipleNonAzureKeyPairsIsValid() {
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        List<ConfigKeyPair> keyPairs = new ArrayList<>();

        keyPairs.add(mock(DirectKeyPair.class));
        keyPairs.add(mock(FilesystemKeyPair.class));

        when(keyConfiguration.getKeyData()).thenReturn(keyPairs);
        when(keyConfiguration.getAzureKeyVaultConfig()).thenReturn(null);

        assertThat(validator.isValid(keyConfiguration, context)).isTrue();
    }

    @Test
    public void hashicorpConfigWithHashicorpKeyPairIsValid() {
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        HashicorpVaultKeyPair keyPair = mock(HashicorpVaultKeyPair.class);
        DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);

        when(keyConfiguration.getKeyData()).thenReturn(Collections.singletonList(keyPair));
        when(keyConfiguration.getKeyVaultConfig(KeyVaultType.HASHICORP)).thenReturn(Optional.of(keyVaultConfig));

        assertThat(validator.isValid(keyConfiguration, context)).isTrue();
    }

    @Test
    public void hashicorpConfigWithMultipleHashicorpKeyPairsIsValid() {
        List<ConfigKeyPair> keyPairs = new ArrayList<>();
        keyPairs.add(mock(HashicorpVaultKeyPair.class));
        keyPairs.add(mock(HashicorpVaultKeyPair.class));

        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);

        when(keyConfiguration.getKeyData()).thenReturn(keyPairs);
        when(keyConfiguration.getKeyVaultConfig(KeyVaultType.HASHICORP)).thenReturn(Optional.of(keyVaultConfig));

        assertThat(validator.isValid(keyConfiguration, context)).isTrue();
    }

    @Test
    public void hashicorpConfigWithMultipleKeyPairTypesIncludingHashicorpIsValid() {
        List<ConfigKeyPair> keyPairs = new ArrayList<>();
        keyPairs.add(mock(HashicorpVaultKeyPair.class));
        keyPairs.add(mock(DirectKeyPair.class));

        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);

        when(keyConfiguration.getKeyData()).thenReturn(keyPairs);
        when(keyConfiguration.getKeyVaultConfig(KeyVaultType.HASHICORP)).thenReturn(Optional.of(keyVaultConfig));

        assertThat(validator.isValid(keyConfiguration, context)).isTrue();
    }

    @Test
    public void hashicorpConfigWithNonHashicorpKeyPairIsValid() {
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        DirectKeyPair keyPair = mock(DirectKeyPair.class);
        DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);

        when(keyConfiguration.getKeyData()).thenReturn(Collections.singletonList(keyPair));
        when(keyConfiguration.getKeyVaultConfig(KeyVaultType.HASHICORP)).thenReturn(Optional.of(keyVaultConfig));

        assertThat(validator.isValid(keyConfiguration, context)).isTrue();
    }

    @Test
    public void hashicorpConfigWithMultipleNonHashicorpKeyPairsIsValid() {
        List<ConfigKeyPair> keyPairs = new ArrayList<>();
        keyPairs.add(mock(DirectKeyPair.class));
        keyPairs.add(mock(InlineKeypair.class));

        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);

        when(keyConfiguration.getKeyData()).thenReturn(keyPairs);
        when(keyConfiguration.getKeyVaultConfig(KeyVaultType.HASHICORP)).thenReturn(Optional.of(keyVaultConfig));

        assertThat(validator.isValid(keyConfiguration, context)).isTrue();
    }

    @Test
    public void noHashicorpConfigWithHashicorpKeyPairIsInvalid() {
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        HashicorpVaultKeyPair keyPair = mock(HashicorpVaultKeyPair.class);

        when(keyConfiguration.getKeyData()).thenReturn(Collections.singletonList(keyPair));
        when(keyConfiguration.getHashicorpKeyVaultConfig()).thenReturn(null);

        assertThat(validator.isValid(keyConfiguration, context)).isFalse();
        verify(context).buildConstraintViolationWithTemplate("{MatchingKeyVaultConfigsForKeyData.hashicorp.message}");
    }

    @Test
    public void noHashicorpConfigWithMultipleHashicorpKeyPairsIsInvalid() {
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        List<ConfigKeyPair> keyPairs = new ArrayList<>();

        keyPairs.add(mock(HashicorpVaultKeyPair.class));
        keyPairs.add(mock(HashicorpVaultKeyPair.class));

        when(keyConfiguration.getKeyData()).thenReturn(keyPairs);
        when(keyConfiguration.getHashicorpKeyVaultConfig()).thenReturn(null);

        assertThat(validator.isValid(keyConfiguration, context)).isFalse();
        verify(context).buildConstraintViolationWithTemplate("{MatchingKeyVaultConfigsForKeyData.hashicorp.message}");
    }

    @Test
    public void noHashicorpConfigWithMultipleKeyPairsIncludingHashicorpIsInvalid() {
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        List<ConfigKeyPair> keyPairs = new ArrayList<>();

        keyPairs.add(mock(HashicorpVaultKeyPair.class));
        keyPairs.add(mock(DirectKeyPair.class));

        when(keyConfiguration.getKeyData()).thenReturn(keyPairs);
        when(keyConfiguration.getHashicorpKeyVaultConfig()).thenReturn(null);

        assertThat(validator.isValid(keyConfiguration, context)).isFalse();
        verify(context).buildConstraintViolationWithTemplate("{MatchingKeyVaultConfigsForKeyData.hashicorp.message}");
    }

    @Test
    public void noHashicorpConfigWithNonHashicorpKeyPairIsValid() {
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        DirectKeyPair keyPair = mock(DirectKeyPair.class);

        when(keyConfiguration.getKeyData()).thenReturn(Collections.singletonList(keyPair));
        when(keyConfiguration.getHashicorpKeyVaultConfig()).thenReturn(null);

        assertThat(validator.isValid(keyConfiguration, context)).isTrue();
    }

    @Test
    public void noHashicorpConfigWithMultipleNonHashicorpKeyPairsIsValid() {
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        List<ConfigKeyPair> keyPairs = new ArrayList<>();

        keyPairs.add(mock(DirectKeyPair.class));
        keyPairs.add(mock(FilesystemKeyPair.class));

        when(keyConfiguration.getKeyData()).thenReturn(keyPairs);
        when(keyConfiguration.getHashicorpKeyVaultConfig()).thenReturn(null);

        assertThat(validator.isValid(keyConfiguration, context)).isTrue();
    }

    @Test
    public void azureConfigWithHashicorpKeyPairIsInvalid() {
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        List<ConfigKeyPair> keyPairs = new ArrayList<>();

        keyPairs.add(mock(HashicorpVaultKeyPair.class));

        when(keyConfiguration.getKeyData()).thenReturn(keyPairs);
        AzureKeyVaultConfig azureConfig = mock(AzureKeyVaultConfig.class);
        when(keyConfiguration.getAzureKeyVaultConfig()).thenReturn(azureConfig);

        assertThat(validator.isValid(keyConfiguration, context)).isFalse();
        verify(context).buildConstraintViolationWithTemplate("{MatchingKeyVaultConfigsForKeyData.hashicorp.message}");
    }

    @Test
    public void hashicorpConfigWithAzureKeyPairIsInvalid() {
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        List<ConfigKeyPair> keyPairs = new ArrayList<>();

        keyPairs.add(mock(AzureVaultKeyPair.class));

        when(keyConfiguration.getKeyData()).thenReturn(keyPairs);
        HashicorpKeyVaultConfig hashicorpConfig = mock(HashicorpKeyVaultConfig.class);
        when(keyConfiguration.getHashicorpKeyVaultConfig()).thenReturn(hashicorpConfig);

        assertThat(validator.isValid(keyConfiguration, context)).isFalse();
        verify(context).buildConstraintViolationWithTemplate("{MatchingKeyVaultConfigsForKeyData.azure.message}");
    }

    @Test
    public void noAzureConfigWithAzureDefaultKeyVaultConfigIsValid() {
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        AzureVaultKeyPair keyPair = mock(AzureVaultKeyPair.class);
        DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);
        when(keyVaultConfig.getKeyVaultType()).thenReturn(KeyVaultType.AZURE);

        when(keyConfiguration.getKeyData()).thenReturn(Collections.singletonList(keyPair));
        when(keyConfiguration.getKeyVaultConfig(KeyVaultType.AZURE)).thenReturn(Optional.of(keyVaultConfig));

        assertThat(validator.isValid(keyConfiguration, context)).isTrue();
    }

    @Test
    public void noAzureConfigWithHashicorpDefaultKeyVaultConfigIsInvalid() {
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        AzureVaultKeyPair keyPair = mock(AzureVaultKeyPair.class);
        DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);
        when(keyVaultConfig.getKeyVaultType()).thenReturn(KeyVaultType.HASHICORP);

        when(keyConfiguration.getKeyData()).thenReturn(Collections.singletonList(keyPair));
        when(keyConfiguration.getKeyVaultConfig(KeyVaultType.HASHICORP)).thenReturn(Optional.of(keyVaultConfig));

        assertThat(validator.isValid(keyConfiguration, context)).isFalse();
    }

    @Test
    public void noHashicorpConfigWithHashicorpDefaultKeyVaultConfigIsValid() {
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        HashicorpVaultKeyPair keyPair = mock(HashicorpVaultKeyPair.class);
        DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);
        when(keyVaultConfig.getKeyVaultType()).thenReturn(KeyVaultType.HASHICORP);

        when(keyConfiguration.getKeyData()).thenReturn(Collections.singletonList(keyPair));
        when(keyConfiguration.getKeyVaultConfig(KeyVaultType.HASHICORP)).thenReturn(Optional.of(keyVaultConfig));

        assertThat(validator.isValid(keyConfiguration, context)).isTrue();
    }

    @Test
    public void noHashicorpConfigWithAzureDefaultKeyVaultConfigIsInvalid() {
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        HashicorpVaultKeyPair keyPair = mock(HashicorpVaultKeyPair.class);
        DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);
        when(keyVaultConfig.getKeyVaultType()).thenReturn(KeyVaultType.AZURE);

        when(keyConfiguration.getKeyData()).thenReturn(Collections.singletonList(keyPair));
        when(keyConfiguration.getKeyVaultConfig(KeyVaultType.AZURE)).thenReturn(Optional.of(keyVaultConfig));

        assertThat(validator.isValid(keyConfiguration, context)).isFalse();
    }

    @Test
    public void allConstraintViolationsAreRaised() {
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);

        ConfigKeyPair azure = mock(AzureVaultKeyPair.class);
        ConfigKeyPair hashicorp = mock(HashicorpVaultKeyPair.class);
        ConfigKeyPair aws = mock(AWSKeyPair.class);

        when(keyConfiguration.getKeyData()).thenReturn(Arrays.asList(azure, hashicorp, aws));

        assertThat(validator.isValid(keyConfiguration, context)).isFalse();
        verify(context).buildConstraintViolationWithTemplate("{MatchingKeyVaultConfigsForKeyData.azure.message}");
        verify(context).buildConstraintViolationWithTemplate("{MatchingKeyVaultConfigsForKeyData.aws.message}");
        verify(context).buildConstraintViolationWithTemplate("{MatchingKeyVaultConfigsForKeyData.hashicorp.message}");
    }
}
