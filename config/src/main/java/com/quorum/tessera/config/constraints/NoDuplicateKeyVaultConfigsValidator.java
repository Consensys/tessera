package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.KeyVaultConfig;
import com.quorum.tessera.config.KeyVaultType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.*;
import java.util.stream.Collectors;

public class NoDuplicateKeyVaultConfigsValidator
    implements ConstraintValidator<NoDuplicateKeyVaultConfigs, KeyConfiguration> {

  private NoDuplicateKeyVaultConfigs config;

  @Override
  public void initialize(NoDuplicateKeyVaultConfigs config) {
    this.config = config;
  }

  @Override
  public boolean isValid(
      KeyConfiguration keyConfiguration, ConstraintValidatorContext constraintValidatorContext) {
    // cannot have duplicates if the KeyVaultConfigs list is empty
    if (Objects.isNull(keyConfiguration)) {
      return true;
    }

    if (Objects.isNull(keyConfiguration.getKeyVaultConfigs())) {
      return true;
    }

    if (keyConfiguration.getKeyVaultConfigs().isEmpty()) {
      return true;
    }

    final List<KeyVaultConfig> legacyConfigs = new ArrayList<>();
    legacyConfigs.add(keyConfiguration.getHashicorpKeyVaultConfig());
    legacyConfigs.add(keyConfiguration.getAzureKeyVaultConfig());

    List<KeyVaultConfig> configs =
        keyConfiguration.getKeyVaultConfigs().stream()
            .map(KeyVaultConfig.class::cast)
            .collect(Collectors.toList());
    configs.addAll(legacyConfigs);

    final Map<KeyVaultType, Integer> typeCount =
        configs.stream()
            .filter(Objects::nonNull)
            .filter(c -> Objects.nonNull(c.getKeyVaultType()))
            .collect(Collectors.toMap(e -> e.getKeyVaultType(), v -> 1, (l, r) -> l + 1));

    typeCount.entrySet().stream()
        .filter(e -> e.getValue() > 1)
        .map(e -> e.getKey().name())
        .forEach(
            s -> {
              String message =
                  String.join(
                      " ", s, constraintValidatorContext.getDefaultConstraintMessageTemplate());
              constraintValidatorContext.disableDefaultConstraintViolation();
              constraintValidatorContext
                  .buildConstraintViolationWithTemplate(message)
                  .addConstraintViolation();
            });

    return typeCount.values().stream().allMatch(v -> v == 1);
  }
}
