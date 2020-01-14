package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.KeyVaultConfig;
import com.quorum.tessera.config.KeyVaultType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class NoDuplicateKeyVaultConfigsValidator
        implements ConstraintValidator<NoDuplicateKeyVaultConfigs, KeyConfiguration> {

    private NoDuplicateKeyVaultConfigs config;

    @Override
    public void initialize(NoDuplicateKeyVaultConfigs config) {
        this.config = config;
    }

    @Override
    public boolean isValid(KeyConfiguration keyConfiguration, ConstraintValidatorContext constraintValidatorContext) {
        // cannot have duplicates if the KeyVaultConfigs list is empty
        if (keyConfiguration == null
                || keyConfiguration.getKeyVaultConfigs() == null
                || keyConfiguration.getKeyVaultConfigs().isEmpty()) {
            return true;
        }


        final List<KeyVaultConfig> legacyConfigs = new ArrayList<>();
        legacyConfigs.add(keyConfiguration.getHashicorpKeyVaultConfig());
        legacyConfigs.add(keyConfiguration.getAzureKeyVaultConfig());

        List<KeyVaultConfig> configs = keyConfiguration.getKeyVaultConfigs().stream()
            .map(KeyVaultConfig.class::cast).collect(Collectors.toList());
        configs.addAll(legacyConfigs);
        
        final Map<KeyVaultType, Integer> typeCount = configs.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(e -> e.getKeyVaultType(),v -> 1, (l, r) -> l + 1));

        List<String> constraintMessages = typeCount.entrySet().stream()
            .filter(e -> e.getValue() > 1)
            .map(e -> e.getKey().name())
            .map(s -> String.join(" ",s,constraintValidatorContext.getDefaultConstraintMessageTemplate()))
            .collect(Collectors.toList());

            constraintMessages.forEach(message -> {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext
                    .buildConstraintViolationWithTemplate(message)
                    .addConstraintViolation();
            });

            return constraintMessages.isEmpty();

    }
}
