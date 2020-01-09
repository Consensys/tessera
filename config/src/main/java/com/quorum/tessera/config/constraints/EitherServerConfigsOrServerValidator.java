package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.Config;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EitherServerConfigsOrServerValidator
        implements ConstraintValidator<ValidEitherServerConfigsOrServer, Config> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EitherServerConfigsOrServerValidator.class);

    @Override
    public boolean isValid(Config config, ConstraintValidatorContext constraintContext) {
        if (config == null) {
            return true;
        }

        if (null == config.getServer() && config.isServerConfigsNull()) {
            LOGGER.debug("One of server/serverConfigs must be provided.");
            constraintContext.disableDefaultConstraintViolation();
            constraintContext
                    .buildConstraintViolationWithTemplate("One of server/serverConfigs must be provided.")
                    .addConstraintViolation();
            return false;
        }

        if (null != config.getServer() && !config.isServerConfigsNull()) {
            LOGGER.debug("Either one of server/serverConfigs can be configured (not both).");
            constraintContext.disableDefaultConstraintViolation();
            constraintContext
                    .buildConstraintViolationWithTemplate(
                            "Either one of server/serverConfigs can be configured (not both).")
                    .addConstraintViolation();
            return false;
        }

        if (Objects.nonNull(config.getServer()) && Objects.isNull(config.getUnixSocketFile())) {
            LOGGER.debug("Unix socket file must be configured is using deprecated server config");
            constraintContext.disableDefaultConstraintViolation();
            constraintContext
                    .buildConstraintViolationWithTemplate(
                            "Unix socket file must be configured is using deprecated server config. Check config.unixSocketFile")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
