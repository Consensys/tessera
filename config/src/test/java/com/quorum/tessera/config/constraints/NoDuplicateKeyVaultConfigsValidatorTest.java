package com.quorum.tessera.config.constraints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.*;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NoDuplicateKeyVaultConfigsValidatorTest {

  private ConstraintValidatorContext constraintValidatorContext;

  private NoDuplicateKeyVaultConfigsValidator validator;

  private NoDuplicateKeyVaultConfigs annotation;

  @Before
  public void setUp() {
    validator = new NoDuplicateKeyVaultConfigsValidator();
    constraintValidatorContext = mock(ConstraintValidatorContext.class);
    annotation = mock(NoDuplicateKeyVaultConfigs.class);
    validator.initialize(annotation);
  }

  @After
  public void tearDown() {

    verifyNoMoreInteractions(annotation);
  }

  @Test
  public void nullKeyConfigurationIsIgnored() {
    boolean result = validator.isValid(null, constraintValidatorContext);
    assertThat(result).isTrue();
  }

  @Test
  public void nullKeyVaultConfigsIsIgnored() {

    KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    when(keyConfiguration.getKeyVaultConfigs()).thenReturn(null);
    boolean result = validator.isValid(keyConfiguration, constraintValidatorContext);
    assertThat(result).isTrue();
  }

  @Test
  public void emptyKeyVaultConfigsIsIgnored() {

    KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    when(keyConfiguration.getKeyVaultConfigs()).thenReturn(Collections.EMPTY_LIST);

    boolean result = validator.isValid(keyConfiguration, constraintValidatorContext);
    assertThat(result).isTrue();
  }

  @Test
  public void singleAzureConfigValid() {
    DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);
    when(keyVaultConfig.getKeyVaultType()).thenReturn(KeyVaultType.AZURE);

    List<KeyVaultConfig> configs = Collections.singletonList(keyVaultConfig);

    KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    when(keyConfiguration.getKeyVaultConfigs()).thenReturn(configs);

    boolean result = validator.isValid(keyConfiguration, constraintValidatorContext);
    assertThat(result).isTrue();
  }

  @Test
  public void singleHashicorpConfigValid() {
    DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);
    when(keyVaultConfig.getKeyVaultType()).thenReturn(KeyVaultType.HASHICORP);

    List<KeyVaultConfig> configs = Collections.singletonList(keyVaultConfig);

    KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    when(keyConfiguration.getKeyVaultConfigs()).thenReturn(configs);

    boolean result = validator.isValid(keyConfiguration, constraintValidatorContext);
    assertThat(result).isTrue();
  }

  @Test
  public void singleAWSConfigValid() {
    DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);
    when(keyVaultConfig.getKeyVaultType()).thenReturn(KeyVaultType.AWS);

    List<KeyVaultConfig> configs = Collections.singletonList(keyVaultConfig);

    KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    when(keyConfiguration.getKeyVaultConfigs()).thenReturn(configs);

    boolean result = validator.isValid(keyConfiguration, constraintValidatorContext);
    assertThat(result).isTrue();
  }

  @Test
  public void multiConfigsDifferentTypesValid() {
    DefaultKeyVaultConfig azure = mock(DefaultKeyVaultConfig.class);
    when(azure.getKeyVaultType()).thenReturn(KeyVaultType.AZURE);

    DefaultKeyVaultConfig hashicorp = mock(DefaultKeyVaultConfig.class);
    when(hashicorp.getKeyVaultType()).thenReturn(KeyVaultType.HASHICORP);

    DefaultKeyVaultConfig aws = mock(DefaultKeyVaultConfig.class);
    when(aws.getKeyVaultType()).thenReturn(KeyVaultType.AWS);

    List<KeyVaultConfig> configs = Arrays.asList(azure, hashicorp, aws);

    KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    when(keyConfiguration.getKeyVaultConfigs()).thenReturn(configs);

    boolean result = validator.isValid(keyConfiguration, constraintValidatorContext);
    assertThat(result).isTrue();
  }

  @Test
  public void multiConfigsSameTypesInvalid() {
    ConstraintValidatorContext.ConstraintViolationBuilder builder =
        mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);

    String defaultMessageTemplate = "DEFAULT_MESSAGE_TEMPLATE";
    when(constraintValidatorContext.getDefaultConstraintMessageTemplate())
        .thenReturn(defaultMessageTemplate);

    when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString()))
        .thenReturn(builder);

    DefaultKeyVaultConfig azure = mock(DefaultKeyVaultConfig.class);
    when(azure.getKeyVaultType()).thenReturn(KeyVaultType.AZURE);

    DefaultKeyVaultConfig hashicorp = mock(DefaultKeyVaultConfig.class);
    when(hashicorp.getKeyVaultType()).thenReturn(KeyVaultType.HASHICORP);

    DefaultKeyVaultConfig aws = mock(DefaultKeyVaultConfig.class);
    when(aws.getKeyVaultType()).thenReturn(KeyVaultType.AWS);

    DefaultKeyVaultConfig azure2 = mock(DefaultKeyVaultConfig.class);
    when(azure2.getKeyVaultType()).thenReturn(KeyVaultType.AZURE);

    DefaultKeyVaultConfig hashicorp2 = mock(DefaultKeyVaultConfig.class);
    when(hashicorp2.getKeyVaultType()).thenReturn(KeyVaultType.HASHICORP);

    DefaultKeyVaultConfig aws2 = mock(DefaultKeyVaultConfig.class);
    when(aws2.getKeyVaultType()).thenReturn(KeyVaultType.AWS);

    List<KeyVaultConfig> configs = Arrays.asList(azure, hashicorp, aws, azure2, hashicorp2, aws2);

    KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    when(keyConfiguration.getKeyVaultConfigs()).thenReturn(configs);

    boolean result = validator.isValid(keyConfiguration, constraintValidatorContext);
    assertThat(result).isFalse();

    verify(constraintValidatorContext)
        .buildConstraintViolationWithTemplate("AZURE " + defaultMessageTemplate);
    verify(constraintValidatorContext)
        .buildConstraintViolationWithTemplate("HASHICORP " + defaultMessageTemplate);
    verify(constraintValidatorContext)
        .buildConstraintViolationWithTemplate("AWS " + defaultMessageTemplate);
  }

  @Test
  public void sameKeyVaultConfigTypesInBothDeprecatedAndGenericInvalid() {
    when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString()))
        .thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder.class));

    String defaultMessageTemplate = "DEFAULT_MESSAGE_TEMPLATE";
    when(constraintValidatorContext.getDefaultConstraintMessageTemplate())
        .thenReturn(defaultMessageTemplate);

    DefaultKeyVaultConfig azureGeneric = mock(DefaultKeyVaultConfig.class);
    when(azureGeneric.getKeyVaultType()).thenReturn(KeyVaultType.AZURE);

    DefaultKeyVaultConfig hashicorpGeneric = mock(DefaultKeyVaultConfig.class);
    when(hashicorpGeneric.getKeyVaultType()).thenReturn(KeyVaultType.HASHICORP);

    AzureKeyVaultConfig azureDeprecated = mock(AzureKeyVaultConfig.class);
    when(azureDeprecated.getKeyVaultType()).thenReturn(KeyVaultType.AZURE);

    HashicorpKeyVaultConfig hashicorpDeprecated = mock(HashicorpKeyVaultConfig.class);
    when(hashicorpDeprecated.getKeyVaultType()).thenReturn(KeyVaultType.HASHICORP);

    List<KeyVaultConfig> configs = Arrays.asList(azureGeneric, hashicorpGeneric);

    KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    when(keyConfiguration.getKeyVaultConfigs()).thenReturn(configs);

    when(keyConfiguration.getAzureKeyVaultConfig()).thenReturn(azureDeprecated);
    when(keyConfiguration.getHashicorpKeyVaultConfig()).thenReturn(hashicorpDeprecated);

    boolean result = validator.isValid(keyConfiguration, constraintValidatorContext);

    assertThat(result).isFalse();
    verify(constraintValidatorContext)
        .buildConstraintViolationWithTemplate("AZURE " + defaultMessageTemplate);
    verify(constraintValidatorContext)
        .buildConstraintViolationWithTemplate("HASHICORP " + defaultMessageTemplate);
  }

  @Test
  public void nullKeyVaultConfigTypeIsHandled() {
    KeyConfiguration keyConfiguration = new KeyConfiguration();
    DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);
    when(keyVaultConfig.getKeyVaultType()).thenReturn(null);

    keyConfiguration.addKeyVaultConfig(keyVaultConfig);

    assertThat(validator.isValid(keyConfiguration, constraintValidatorContext)).isTrue();
  }

  @Test
  public void multipleNullKeyVaultConfigTypesIsHandled() {
    KeyConfiguration keyConfiguration = new KeyConfiguration();
    DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);
    when(keyVaultConfig.getKeyVaultType()).thenReturn(null);

    keyConfiguration.addKeyVaultConfig(keyVaultConfig);
    keyConfiguration.addKeyVaultConfig(keyVaultConfig);

    assertThat(validator.isValid(keyConfiguration, constraintValidatorContext)).isTrue();
  }
}
