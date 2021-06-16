package com.quorum.tessera.config.cli;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.*;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class DispatchingKeyVaultHandlerTest {

  private KeyVaultConfigOptions configOptions;

  private Class<? extends KeyVaultConfig> resultType;

  public DispatchingKeyVaultHandlerTest(Map<String, ?> config) {
    this.configOptions = (KeyVaultConfigOptions) config.get("configOptions");
    this.resultType = (Class<? extends KeyVaultConfig>) config.get("resultType");
  }

  private DispatchingKeyVaultHandler dispatchingKeyVaultHandler;

  @Before
  public void beforeTest() {
    dispatchingKeyVaultHandler = new DispatchingKeyVaultHandler();
  }

  @Test
  public void handle() {
    KeyVaultConfig keyVaultConfig = dispatchingKeyVaultHandler.handle(configOptions);
    assertThat(keyVaultConfig).isExactlyInstanceOf(resultType);
  }

  @Parameterized.Parameters(name = "{0}")
  public static List<Map<String, ?>> configs() {
    return List.of(
        Map.of("resultType", DefaultKeyVaultConfig.class),
        Map.of(
            "resultType",
            DefaultKeyVaultConfig.class,
            "configOptions",
            new KeyVaultConfigOptions() {
              @Override
              public KeyVaultType getVaultType() {
                return KeyVaultType.AWS;
              }
            }),
        Map.of(
            "resultType",
            HashicorpKeyVaultConfig.class,
            "configOptions",
            new KeyVaultConfigOptions() {
              @Override
              public KeyVaultType getVaultType() {
                return KeyVaultType.HASHICORP;
              }
            }),
        Map.of(
            "resultType",
            AzureKeyVaultConfig.class,
            "configOptions",
            new KeyVaultConfigOptions() {
              @Override
              public KeyVaultType getVaultType() {
                return KeyVaultType.AZURE;
              }
            }));
  }
}
