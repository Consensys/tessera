package com.quorum.tessera.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.quorum.tessera.config.util.JaxbUtil;
import org.junit.Test;

public class DefaultKeyVaultConfigTest {

  @Test
  public void defaultInstance() {
    DefaultKeyVaultConfig defaultKeyVaultConfig = new DefaultKeyVaultConfig();
    assertThat(defaultKeyVaultConfig.getKeyVaultType()).isNull();
    assertThat(defaultKeyVaultConfig.getProperties()).isEmpty();
  }

  @Test
  public void keyVaultConfigDefaultGetPropertiesIsEmpty() {
    KeyVaultConfig keyVaultConfig = mock(KeyVaultConfig.class);
    assertThat(keyVaultConfig.getKeyVaultType()).isNull();
    assertThat(keyVaultConfig.getProperties()).isEmpty();
  }

  @Test
  public void doStuff() {
    DefaultKeyVaultConfig defaultKeyVaultConfig = new DefaultKeyVaultConfig();
    defaultKeyVaultConfig.setKeyVaultType(KeyVaultType.HASHICORP);
    defaultKeyVaultConfig.setProperty("url", "someurl");
    JaxbUtil.marshalWithNoValidation(defaultKeyVaultConfig, System.out);
  }
}
