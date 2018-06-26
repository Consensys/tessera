package com.github.nexus.config;

import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.test.impl.DefaultValuesNullTester;
import com.openpojo.validation.test.impl.GetterTester;
import org.junit.Test;

public class OpenPojoTest {

    public OpenPojoTest() {
    }

    @Test
    public void executeOpenPojoValidations() {

        Validator pojoValidator = ValidatorBuilder.create()
                .with(new GetterMustExistRule())
                .with(new GetterTester())
                .with(new DefaultValuesNullTester())
                .build();

        
        pojoValidator.validate(getClass().getPackage().getName(),
                (pc) -> !pc.getClazz().isAssignableFrom(ObjectFactory.class) 
                        && !pc.getClazz().isAssignableFrom(JaxbConfigFactory.class)
                        && !pc.getClazz().getSimpleName().contains("Test"));

    }
    
    
    
}
