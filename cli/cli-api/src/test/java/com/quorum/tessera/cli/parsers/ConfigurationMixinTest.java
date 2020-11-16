package com.quorum.tessera.cli.parsers;

import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import org.junit.Test;


public class ConfigurationMixinTest {

    @Test
    public void pojoTest() {
        ValidatorBuilder.create()
                .with(new GetterTester())
                .with(new SetterTester())
                .build()
                .validate(PojoClassFactory.getPojoClass(ConfigurationMixin.class));
    }
}
