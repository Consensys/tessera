package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.ServerConfig;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerConfigValidator implements ConstraintValidator<ValidServerConfig, ServerConfig> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerConfigValidator.class);

  @Override
  public boolean isValid(ServerConfig serverConfig, ConstraintValidatorContext constraintContext) {

    if (serverConfig == null) {
      return true;
    }

    if (serverConfig.getApp() == null
        || AppType.ADMIN.toString().equals(serverConfig.getApp().toString())) {
      String supportedAppTypes =
          Arrays.stream(AppType.values())
              .filter(t -> t != AppType.ADMIN)
              .map(AppType::name)
              .map(n -> "THIRD_PARTY".equals(n) ? "ThirdParty" : n)
              .collect(Collectors.joining(", "));

      constraintContext.disableDefaultConstraintViolation();
      constraintContext
          .buildConstraintViolationWithTemplate(
              "app must be provided for serverConfig and be one of " + supportedAppTypes)
          .addConstraintViolation();
      return false;
    }

    if (serverConfig.getApp() != AppType.THIRD_PARTY) {
      if (serverConfig.getCrossDomainConfig() != null) {
        LOGGER.debug(
            "Invalid server config. CrossDomainConfig is only allowed in ThirdParty server");
        constraintContext.disableDefaultConstraintViolation();
        constraintContext
            .buildConstraintViolationWithTemplate(
                "Invalid server config. CrossDomainConfig is only allowed in ThirdParty server")
            .addConstraintViolation();
        return false;
      }
    }

    return true;
  }
}
