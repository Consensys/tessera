package com.quorum.tessera.config.constraints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.quorum.tessera.config.DefaultKeyVaultConfig;
import com.quorum.tessera.config.KeyVaultType;
import jakarta.validation.ConstraintValidatorContext;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;

public class KeyVaultConfigValidatorTest {

  private KeyVaultConfigValidator keyVaultConfigValidator;

  private ConstraintValidatorContext context;

  @Before
  public void setUp() {
    context = mock(ConstraintValidatorContext.class);

    ConstraintValidatorContext.ConstraintViolationBuilder builder =
        mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
    when(context.buildConstraintViolationWithTemplate(any(String.class))).thenReturn(builder);

    keyVaultConfigValidator = new KeyVaultConfigValidator();

    ValidKeyVaultConfig validKeyVaultConfig = mock(ValidKeyVaultConfig.class);
    keyVaultConfigValidator.initialize(validKeyVaultConfig);
  }

  @Test
  public void nullKeyConfigurationIsAllowedAndWillBePickedUpByNotNullAnnotation() {
    assertThat(keyVaultConfigValidator.isValid(null, context)).isTrue();
  }

  @Test
  public void nullKeyVaultTypeIsAllowedAndWillBePickedUpByNotNullAnnotation() {
    DefaultKeyVaultConfig config = new DefaultKeyVaultConfig();

    assertThat(keyVaultConfigValidator.isValid(config, context)).isTrue();
  }

  @Test
  public void validAzureConfig() {

    DefaultKeyVaultConfig config = new DefaultKeyVaultConfig();
    config.setKeyVaultType(KeyVaultType.AZURE);
    config.setProperty("url", "someurl");

    assertThat(keyVaultConfigValidator.isValid(config, context)).isTrue();
  }

  @Test
  public void invalidAzureConfig() {

    DefaultKeyVaultConfig config = new DefaultKeyVaultConfig();
    config.setKeyVaultType(KeyVaultType.AZURE);

    assertThat(keyVaultConfigValidator.isValid(config, context)).isFalse();
  }

  @Test
  public void validHashicorpConfig() throws Exception {

    Path somePath = Files.createTempFile(UUID.randomUUID().toString(), ".txt");
    somePath.toFile().deleteOnExit();
    DefaultKeyVaultConfig config = new DefaultKeyVaultConfig();
    config.setKeyVaultType(KeyVaultType.HASHICORP);
    config.setProperty("url", "someurl");
    config.setProperty("tlsKeyStorePath", somePath.toString());
    config.setProperty("tlsTrustStorePath", somePath.toString());

    assertThat(keyVaultConfigValidator.isValid(config, context)).isTrue();
  }

  @Test
  public void invalidHashicorpConfig() {

    Path somePath = mock(Path.class);

    DefaultKeyVaultConfig config = new DefaultKeyVaultConfig();
    config.setKeyVaultType(KeyVaultType.HASHICORP);

    config.setProperty("tlsKeyStorePath", somePath.toString());
    config.setProperty("tlsTrustStorePath", somePath.toString());

    assertThat(keyVaultConfigValidator.isValid(config, context)).isFalse();
  }

  @Test
  public void validAWSConfig() {
    DefaultKeyVaultConfig config = new DefaultKeyVaultConfig();
    config.setKeyVaultType(KeyVaultType.AWS);
    config.setProperty("endpoint", "http://someurl");

    assertThat(keyVaultConfigValidator.isValid(config, context)).isTrue();
  }

  @Test
  public void validAWSConfigNoEndpoint() {
    DefaultKeyVaultConfig config = new DefaultKeyVaultConfig();
    config.setKeyVaultType(KeyVaultType.AWS);

    assertThat(keyVaultConfigValidator.isValid(config, context)).isTrue();
  }

  @Test
  public void invalidAWSConfig() {
    DefaultKeyVaultConfig config = new DefaultKeyVaultConfig();
    config.setKeyVaultType(KeyVaultType.AWS);
    config.setProperty("endpoint", "noscheme");

    assertThat(keyVaultConfigValidator.isValid(config, context)).isFalse();
  }
}
