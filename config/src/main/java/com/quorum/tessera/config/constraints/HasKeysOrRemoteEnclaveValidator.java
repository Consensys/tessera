package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import java.util.Optional;

public class HasKeysOrRemoteEnclaveValidator
    implements ConstraintValidator<HasKeysOrRemoteEnclave, Config> {

  @Override
  public boolean isValid(Config config, ConstraintValidatorContext constraintValidatorContext) {

    return Optional.ofNullable(config.getKeys())
        .map(Objects::nonNull)
        .orElse(config.getServerConfigs().stream().anyMatch(s -> s.getApp() == AppType.ENCLAVE));
  }
}
