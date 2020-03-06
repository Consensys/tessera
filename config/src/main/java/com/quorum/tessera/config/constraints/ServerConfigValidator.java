package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ServerConfigValidator implements ConstraintValidator<ValidServerConfig, ServerConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConfigValidator.class);

    @Override
    public boolean isValid(ServerConfig serverConfig, ConstraintValidatorContext constraintContext) {

        if (serverConfig == null) {
            return true;
        }

        if (serverConfig.getApp() == null) {
            List<String> supportedAppTypes = Arrays.stream(AppType.values())
                .map(AppType::toString)
                .collect(Collectors.toList());

            constraintContext.disableDefaultConstraintViolation();
            constraintContext.buildConstraintViolationWithTemplate("app must be provided for serverConfig and be one of " + String.join(", ", supportedAppTypes))
                .addConstraintViolation();
            return false;
        }

        if (!serverConfig.getApp().getAllowedCommunicationTypes().contains(serverConfig.getCommunicationType())) {
            LOGGER.debug(
                    "Invalid communicationType '"
                            + serverConfig.getCommunicationType()
                            + "' specified for serverConfig with app "
                            + serverConfig.getApp());
            constraintContext.disableDefaultConstraintViolation();
            constraintContext
                    .buildConstraintViolationWithTemplate(
                            "Invalid communicationType '"
                                    + serverConfig.getCommunicationType()
                                    + "' specified for serverConfig with app "
                                    + serverConfig.getApp())
                    .addConstraintViolation();
            return false;
        }

        if (serverConfig.getApp() != AppType.THIRD_PARTY) {
            if (serverConfig.getCrossDomainConfig() != null) {
                LOGGER.debug("Invalid server config. CrossDomainConfig is only allowed in ThirdParty server");
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
