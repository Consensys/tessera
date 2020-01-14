package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.KeyVaultType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MatchingKeyVaultConfigsForKeyDataValidator
        implements ConstraintValidator<MatchingKeyVaultConfigsForKeyData, KeyConfiguration> {

    @Override
    public boolean isValid(KeyConfiguration keyConfiguration, ConstraintValidatorContext cvc) {

        // return true so that only NotNull annotation creates violation
        if (keyConfiguration == null) {
            return true;
        }

        List<Boolean> outcomes = Stream.of(KeyVaultType.values()).map(k -> {

            boolean isUsingKeyVaultType = keyConfiguration.getKeyData().stream()
                .anyMatch(v -> k.getKeyPairType().isInstance(v));

            boolean hasKeyVaultConfig = keyConfiguration.getKeyVaultConfig(k).isPresent();

            if (isUsingKeyVaultType && !hasKeyVaultConfig) {
                cvc.disableDefaultConstraintViolation();
                String messageKey = String.format("{MatchingKeyVaultConfigsForKeyData.%s.message}",k.name().toLowerCase());
                cvc.buildConstraintViolationWithTemplate(messageKey)
                    .addConstraintViolation();
                return false;
            }

            return true;
        }).collect(Collectors.toList());

        return outcomes.stream().allMatch(v -> v);

    }
}
