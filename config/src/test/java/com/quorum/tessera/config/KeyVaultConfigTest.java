package com.quorum.tessera.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class KeyVaultConfigTest {

  @Test
  public void hasAndGetProperty() {

    final Map<String, String> props = new HashMap<>();
    props.put("greeting", "Hellow");

    KeyVaultConfig keyVaultConfig =
        new KeyVaultConfig() {
          @Override
          public KeyVaultType getKeyVaultType() {
            return null;
          }

          @Override
          public Map<String, String> getProperties() {
            return props;
          }
        };

    assertThat(keyVaultConfig.hasProperty("greeting")).isTrue();
    assertThat(keyVaultConfig.getProperty("greeting")).isPresent();
    assertThat(keyVaultConfig.getProperties()).isSameAs(props);
    assertThat(keyVaultConfig.getProperty("greeting").get()).isEqualTo("Hellow");
  }

  @Test
  public void create() {
    KeyVaultConfig keyVaultConfig = KeyVaultConfig.create();
    assertThat(keyVaultConfig).isNotNull().isExactlyInstanceOf(DefaultKeyVaultConfig.class);
    assertThat(keyVaultConfig.getProperties()).isEmpty();
  }

  @Test
  public void defaultProperiesIsEmpty() {
    KeyVaultConfig keyVaultConfig =
        new KeyVaultConfig() {

          @Override
          public KeyVaultType getKeyVaultType() {
            return null;
          }
        };

    assertThat(keyVaultConfig.getProperties()).isEmpty();
  }
}
