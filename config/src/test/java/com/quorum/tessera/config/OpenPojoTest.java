package com.quorum.tessera.config;

import com.openpojo.reflection.PojoClassFilter;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import static nl.jqno.equalsverifier.Warning.NONFINAL_FIELDS;
import static nl.jqno.equalsverifier.Warning.STRICT_INHERITANCE;

public class OpenPojoTest {

    @Test
    public void executeOpenPojoValidations() {
        final Validator pojoValidator =
                ValidatorBuilder.create()
                        .with(new GetterMustExistRule())
                        .with(new GetterTester())
                        .with(new SetterTester())
                        .build();

        final PojoClassFilter[] filters =
                new PojoClassFilter[] {
                    pc -> !pc.getClazz().isAssignableFrom(ObjectFactory.class),
                    pc -> !pc.getClazz().isAssignableFrom(JaxbConfigFactory.class),
                    pc -> !pc.getClazz().isAssignableFrom(ConfigException.class),
                    pc -> !pc.getClazz().getName().contains(ConfigItem.class.getName()),
                    pc -> !pc.getClazz().getSimpleName().contains("Test")
                };

        pojoValidator.validate("com.quorum.tessera.config", filters);
    }

    @Test
    public void equalsAndHashcode() {
        EqualsVerifier.configure()
                .suppress(STRICT_INHERITANCE, NONFINAL_FIELDS)
                .forClass(FeatureToggles.class)
                .verify();
    }
}
