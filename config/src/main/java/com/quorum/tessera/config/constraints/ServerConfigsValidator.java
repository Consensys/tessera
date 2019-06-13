package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class ServerConfigsValidator implements ConstraintValidator<ValidServerConfigs, List<ServerConfig>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConfigsValidator.class);

    @Override
    public boolean isValid(final List<ServerConfig> serverConfigs, final ConstraintValidatorContext constraintContext) {
        if (serverConfigs == null) {
            return true;
        }

        final long p2PEnabledConfigsCount = serverConfigs
            .stream()
            .filter(ServerConfig::isEnabled)
            .filter(sc -> AppType.P2P.equals(sc.getApp()))
            .count();

        if (p2PEnabledConfigsCount != 1) {
            LOGGER.warn("Exactly one P2P server must be configured and enabled.");

            constraintContext.disableDefaultConstraintViolation();
            constraintContext
                .buildConstraintViolationWithTemplate("Exactly one P2P server must be configured and enabled.")
                .addConstraintViolation();

            return false;
        }

        return true;
    }
}
