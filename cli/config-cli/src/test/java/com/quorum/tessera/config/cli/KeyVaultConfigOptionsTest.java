package com.quorum.tessera.config.cli;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.KeyVaultType;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import picocli.CommandLine;

@RunWith(Parameterized.class)
public class KeyVaultConfigOptionsTest {

  private Map config;

  public KeyVaultConfigOptionsTest(Map config) {
    this.config = config;
  }

  @Test
  public void testOption() throws Exception {

    List<String> optionVariations = (List<String>) config.get("options");
    List<String> values = (List<String>) config.get("values");
    String getter = (String) config.get("getter");

    Convertor convertor =
        (Convertor) config.getOrDefault("convertor", (Convertor<String>) value -> value);

    for (String option : optionVariations) {
      KeyVaultConfigOptions keyVaultConfigOptions = new KeyVaultConfigOptions();

      String vals = String.join(",", values);
      new CommandLine(keyVaultConfigOptions).parseArgs(option.concat("=").concat(vals));

      Object result =
          KeyVaultConfigOptions.class.getDeclaredMethod(getter).invoke(keyVaultConfigOptions);

      values.stream()
          .map(convertor::convert)
          .forEach(
              v -> {
                assertThat(result).describedAs("option %s should be %s", option, v).isEqualTo(v);
              });

      List<Method> otherGetters =
          Arrays.stream(KeyVaultConfigOptions.class.getDeclaredMethods())
              .filter(m -> !m.getName().equals(getter))
              .filter(m -> m.getName().startsWith("get"))
              .collect(Collectors.toList());

      for (Method otherGetter : otherGetters) {
        Object o = otherGetter.invoke(keyVaultConfigOptions);
        assertThat(o).describedAs("%s should have returned null", otherGetter.getName()).isNull();
      }
    }
  }

  @Parameterized.Parameters(name = "{0}")
  public static List<Map> configs() {

    List<Map> keyVaultTypes =
        Arrays.stream(KeyVaultType.values())
            .map(
                k ->
                    Map.of(
                        "options",
                        List.of("--vault.type", "-keygenvaulttype"),
                        "getter",
                        "getVaultType",
                        "values",
                        List.of(k.name()),
                        "convertor",
                        (Convertor<KeyVaultType>) v -> KeyVaultType.valueOf(v)))
            .collect(Collectors.toList());

    List<Map> otherConfigs =
        List.of(
            Map.of(
                "options",
                List.of("--vault.hashicorp.tlskeystore", "-keygenvaultkeystore"),
                "getter",
                "getHashicorpTlsKeystore",
                "values",
                List.of("mytlskeystore"),
                "convertor",
                (Convertor<Path>) value -> Paths.get(value)),
            Map.of(
                "options",
                    List.of("--vault.hashicorp.secretenginepath", "-keygenvaultsecretengine"),
                "getter", "getHashicorpSecretEnginePath",
                "values", List.of("mysecretenginepath")),
            Map.of(
                "options", List.of("--vault.hashicorp.approlepath", "-keygenvaultapprole"),
                "getter", "getHashicorpApprolePath",
                "values", List.of("myapprolepath")),
            Map.of(
                "options", List.of("--vault.url", "-keygenvaulturl"),
                "getter", "getVaultUrl",
                "values", List.of("myvaulturl")),
            Map.of(
                "options",
                List.of("--vault.hashicorp.tlstruststore", "-keygenvaulttruststore"),
                "getter",
                "getHashicorpTlsTruststore",
                "values",
                List.of("tlstruststore"),
                "convertor",
                (Convertor<Path>) value -> Paths.get(value)));

    List<Map> all = new ArrayList<>(keyVaultTypes);
    all.addAll(otherConfigs);
    return List.copyOf(all);
  }

  @FunctionalInterface
  interface Convertor<T> {
    T convert(String value);
  }
}
