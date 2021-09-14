package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerConfigsValidator implements ConstraintValidator<ValidServerConfigs, Config> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerConfigsValidator.class);

  @Override
  public boolean isValid(Config config, ConstraintValidatorContext constraintContext) {
    // Note that null serverConfigs are actually prevented by `@ValidEitherServerConfigsOrServer` in
    // Config
    if (null == config || config.isServerConfigsNull()) {
      return true;
    }

    final Map<AppType, Integer> counts =
        config.getServerConfigs().stream()
            .collect(Collectors.toMap(ServerConfig::getApp, v -> 1, (l, r) -> l + 1));

    final int p2PEnabledConfigsCount = counts.getOrDefault(AppType.P2P, 0);
    final int q2TEnabledConfigsCount = counts.getOrDefault(AppType.Q2T, 0);

    if (p2PEnabledConfigsCount != 1) {
      LOGGER.debug("Exactly one P2P server must be configured.");
      constraintContext.disableDefaultConstraintViolation();
      constraintContext
          .buildConstraintViolationWithTemplate("Exactly one P2P server must be configured.")
          .addConstraintViolation();
      return false;
    }

    if (config.isBootstrapNode()) {
      if (q2TEnabledConfigsCount != 0) {
        LOGGER.debug("Q2T server cannot be specified on a bootstrap node.");
        constraintContext.disableDefaultConstraintViolation();
        constraintContext
            .buildConstraintViolationWithTemplate(
                "Q2T server cannot be specified on a bootstrap node.")
            .addConstraintViolation();
        return false;
      }
    } else if (q2TEnabledConfigsCount == 0) {
      LOGGER.debug("At least one Q2T server must be configured or bootstrap mode enabled.");
      constraintContext.disableDefaultConstraintViolation();
      constraintContext
          .buildConstraintViolationWithTemplate(
              "At least one Q2T server must be configured or bootstrap mode enabled.")
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}
