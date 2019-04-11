package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ServerConfigValidator implements ConstraintValidator<ValidServerConfig, ServerConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConfigValidator.class);

    @Override
    public boolean isValid(ServerConfig serverConfig, ConstraintValidatorContext constraintContext) {

        if(serverConfig == null) {
            return true;
        }
        
        if (!serverConfig.getApp().getAllowedCommunicationTypes().contains(serverConfig.getCommunicationType())) {
            LOGGER.debug("Invalid communicationType '" + serverConfig.getCommunicationType() +
                "' specified for serverConfig with app " + serverConfig.getApp());
            constraintContext.disableDefaultConstraintViolation();
            constraintContext.buildConstraintViolationWithTemplate("Invalid communicationType '" +
                serverConfig.getCommunicationType() + "' specified for serverConfig with app " + serverConfig.getApp())
                .addConstraintViolation();
            return false;

        }
        

        return true;
    }
}
