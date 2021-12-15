package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.KeyVaultType;
import com.quorum.tessera.config.util.KeyDataUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

    List<Boolean> outcomes =
        Stream.of(KeyVaultType.values())
            .filter(
                k -> {
                  if (Optional.ofNullable(keyConfiguration.getKeyData()).isPresent()) {
                    return keyConfiguration.getKeyData().stream()
                        .map(KeyDataUtil::getKeyPairTypeFor)
                        .anyMatch(keyType -> Objects.equals(k.getKeyPairType(), keyType));
                  }
                  return false;
                })
            .filter(k -> !keyConfiguration.getKeyVaultConfig(k).isPresent())
            .map(
                k -> {
                  cvc.disableDefaultConstraintViolation();
                  String messageKey =
                      String.format(
                          "{MatchingKeyVaultConfigsForKeyData.%s.message}", k.name().toLowerCase());
                  cvc.buildConstraintViolationWithTemplate(messageKey).addConstraintViolation();
                  return false;
                })
            .collect(Collectors.toList());

    return outcomes.isEmpty();
  }
}
