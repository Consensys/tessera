package com.quorum.tessera.config.constraints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.*;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MatchingKeyVaultConfigsForKeyDataValidatorTest {

  private MatchingKeyVaultConfigsForKeyDataValidator validator;

  private MatchingKeyVaultConfigsForKeyData annotation;

  private ConstraintValidatorContext constraintValidatorContext;

  @Before
  public void onSetup() {
    constraintValidatorContext = mock(ConstraintValidatorContext.class);
    annotation = mock(MatchingKeyVaultConfigsForKeyData.class);
    validator = new MatchingKeyVaultConfigsForKeyDataValidator();
    validator.initialize(annotation);
  }

  @After
  public void onTearDown() {
    verifyNoMoreInteractions(annotation, constraintValidatorContext);
  }

  @Test
  public void isvalid() {

    KeyConfiguration keyConfiguration = new KeyConfiguration();
    KeyData keyData = new KeyData();
    keyConfiguration.setKeyData(Collections.singletonList(keyData));

    boolean result = validator.isValid(keyConfiguration, constraintValidatorContext);

    assertThat(result).isTrue();
  }

  @Test
  public void nullKeyConfigurationIsIgnored() {
    assertThat(validator.isValid(null, constraintValidatorContext)).isTrue();
  }

  @Test
  public void nullKeyDataIsIgnored() {

    KeyConfiguration keyConfiguration = new KeyConfiguration();

    assertThat(validator.isValid(keyConfiguration, constraintValidatorContext)).isTrue();
  }

  @Test
  public void keyDataDoesnotMatchVaultConfig() {

    ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilder =
        mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);

    when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString()))
        .thenReturn(constraintViolationBuilder);

    KeyConfiguration keyConfiguration = new KeyConfiguration();

    KeyData keyData = new KeyData();
    keyData.setAzureVaultPublicKeyId("AzureVaultPublicKeyId");
    keyData.setAzureVaultPrivateKeyId("AzureVaultPrivateKeyId");

    keyConfiguration.setKeyData(Arrays.asList(keyData));

    DefaultKeyVaultConfig keyVaultConfig = new DefaultKeyVaultConfig();
    keyVaultConfig.setKeyVaultType(KeyVaultType.HASHICORP);

    keyConfiguration.addKeyVaultConfig(keyVaultConfig);

    assertThat(validator.isValid(keyConfiguration, constraintValidatorContext)).isFalse();

    verify(constraintValidatorContext).disableDefaultConstraintViolation();
    verify(constraintValidatorContext).buildConstraintViolationWithTemplate(anyString());
  }

  @Test
  public void keyDataDoesMatchVaultConfig() {

    KeyConfiguration keyConfiguration = new KeyConfiguration();

    KeyData keyData = new KeyData();
    keyData.setAzureVaultPublicKeyId("AzureVaultPublicKeyId");
    keyData.setAzureVaultPrivateKeyId("AzureVaultPrivateKeyId");

    keyConfiguration.setKeyData(Arrays.asList(keyData));

    DefaultKeyVaultConfig keyVaultConfig = new DefaultKeyVaultConfig();
    keyVaultConfig.setKeyVaultType(KeyVaultType.AZURE);

    keyConfiguration.addKeyVaultConfig(keyVaultConfig);

    assertThat(validator.isValid(keyConfiguration, constraintValidatorContext)).isTrue();
  }
}
