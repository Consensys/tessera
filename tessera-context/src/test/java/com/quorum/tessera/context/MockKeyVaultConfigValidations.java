package com.quorum.tessera.context;

import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;

import javax.validation.ConstraintViolation;
import java.util.*;

public class MockKeyVaultConfigValidations implements KeyVaultConfigValidations {

    private static ThreadLocal<Set<ConstraintViolation<?>>> mockedResults =
            new ThreadLocal<>() {
                @Override
                protected Set<ConstraintViolation<?>> initialValue() {
                    return new LinkedHashSet<>();
                }
            };

    public static void addConstraintViolation(ConstraintViolation<?> violation) {
        mockedResults.get().add(violation);
    }

    public static void reset() {
        mockedResults.get().clear();
    }

    @Override
    public Set<ConstraintViolation<?>> validate(KeyConfiguration keys, List<ConfigKeyPair> configKeyPairs) {
        return mockedResults.get();
    }
}
