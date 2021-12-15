package com.quorum.tessera.context.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.quorum.tessera.config.DefaultKeyVaultConfig;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.KeyVaultConfig;
import com.quorum.tessera.config.KeyVaultType;
import com.quorum.tessera.config.keypairs.AzureVaultKeyPair;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import jakarta.validation.ConstraintViolation;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

public class DefaultKeyVaultConfigValidationsTest {

  private DefaultKeyVaultConfigValidations validator;

  @Before
  public void onSetUp() {
    validator = new DefaultKeyVaultConfigValidations();
  }

  @Test
  public void noKeyVaultConfigsReturnsEmptySet() {
    KeyConfiguration keyConfiguration = new KeyConfiguration();

    Set<ConstraintViolation<?>> results = validator.validate(keyConfiguration, null);
    assertThat(results).isEmpty();
  }

  @Test
  public void validCase() {

    KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    // Not ideal. Having to use config object in tests to apply validation rules.
    KeyVaultConfig keyVaultConfig =
        new DefaultKeyVaultConfig() {
          {
            setKeyVaultType(KeyVaultType.AZURE);
          }
        };

    List<KeyVaultConfig> keyVaultConfigList = Arrays.asList(mock(KeyVaultConfig.class));
    when(keyConfiguration.getKeyVaultConfigs()).thenReturn(keyVaultConfigList);

    ConfigKeyPair keyPair = new AzureVaultKeyPair("publicKeyId", "privateKeyId", null, null);

    List<ConfigKeyPair> keyPairs = Arrays.asList(keyPair);

    Set<ConstraintViolation<?>> results = validator.validate(keyConfiguration, keyPairs);
    assertThat(results).isEmpty();
  }
}
