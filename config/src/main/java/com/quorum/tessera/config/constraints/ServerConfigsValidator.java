package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class ServerConfigsValidator implements ConstraintValidator<ValidServerConfigs, List<ServerConfig>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConfigsValidator.class);

    @Override
    public boolean isValid(List<ServerConfig> serverConfigs, ConstraintValidatorContext constraintContext) {
        if (serverConfigs == null) {
            return true;
        }

        int p2PEnabledConfigsCount = 0;
        int q2TEnabledConfigsCount = 0;

        for (ServerConfig sc : serverConfigs) {
            if (sc.isEnabled()) {
                switch (sc.getApp()) {
                    case Q2T:
                        q2TEnabledConfigsCount++;
                        break;
                    case P2P:
                        p2PEnabledConfigsCount++;
                        break;
                }
            }
        }

        if (p2PEnabledConfigsCount != 1) {
            LOGGER.debug("Only one P2P server must be configured and enabled.");
            constraintContext.disableDefaultConstraintViolation();
            constraintContext
                    .buildConstraintViolationWithTemplate("Only one P2P server must be configured and enabled.")
                    .addConstraintViolation();
            return false;
        }

        if (q2TEnabledConfigsCount == 0) {
            LOGGER.debug("At least one Q2T server must be configured and enabled.");
            constraintContext.disableDefaultConstraintViolation();
            constraintContext
                    .buildConstraintViolationWithTemplate("At least one Q2T server must be configured and enabled.")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
