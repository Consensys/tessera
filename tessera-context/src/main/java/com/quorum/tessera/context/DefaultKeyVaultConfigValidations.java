package com.quorum.tessera.context;

import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultKeyVaultConfigValidations implements KeyVaultConfigValidations {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultKeyVaultConfigValidations.class);

    private Validator validator =
            Validation.byDefaultProvider().configure().ignoreXmlConfiguration().buildValidatorFactory().getValidator();

    @Override
    public Set<ConstraintViolation<?>> validate(KeyConfiguration keyConfiguration, List<ConfigKeyPair> configKeyPairs) {
        LOGGER.debug("Validating {} {}", keyConfiguration, configKeyPairs);
        if (keyConfiguration.getKeyVaultConfigs() != null) {

            Set<ConstraintViolation<?>> vaultConfigViolations =
                    keyConfiguration.getKeyVaultConfigs().stream()
                            .map(validator::validate)
                            .flatMap(Set::stream)
                            .collect(Collectors.toSet());

            Set<ConstraintViolation<?>> keyPairViolations =
                    configKeyPairs.stream().map(validator::validate).flatMap(Set::stream).collect(Collectors.toSet());

            LOGGER.debug("Validated{} {}", keyConfiguration, configKeyPairs);
            return Stream.concat(vaultConfigViolations.stream(), keyPairViolations.stream())
                    .collect(Collectors.toSet());
        }

        LOGGER.debug("Ignore validations no key vaults defined.");
        return Collections.emptySet();
    }
}
