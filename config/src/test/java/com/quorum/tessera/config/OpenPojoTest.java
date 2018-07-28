package com.quorum.tessera.config;

import com.openpojo.reflection.PojoClassFilter;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.test.impl.DefaultValuesNullTester;
import com.openpojo.validation.test.impl.GetterTester;
import org.junit.Test;

public class OpenPojoTest {

    @Test
    public void executeOpenPojoValidations() {

        final Validator pojoValidator = ValidatorBuilder
            .create()
            .with(new GetterMustExistRule())
            .with(new GetterTester())
            .with(new DefaultValuesNullTester())
            .build();

        final PojoClassFilter[] filters = new PojoClassFilter[]{
            pc -> !pc.getClazz().isAssignableFrom(ObjectFactory.class),
            pc -> !pc.getClazz().isAssignableFrom(JaxbConfigFactory.class),
            pc -> !pc.getClazz().isAssignableFrom(ConfigException.class),
            pc -> !pc.getClazz().getName().contains(ConfigItem.class.getName()),
            pc -> !pc.getClazz().getSimpleName().contains("Test")
        };

        pojoValidator.validate("com.quorum.tessera.config", filters);

    }

}
