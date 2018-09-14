package com.quorum.tessera;

import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.*;
import com.openpojo.validation.test.impl.GetterTester;
import org.junit.Test;

public class OpenPojoEntityTest {



    @Test
    public void executeOpenPojoValidationsNoSetter() {

        final Validator pojoValidator = ValidatorBuilder.create()
            .with(new GetterMustExistRule())
            .with(new GetterTester())
            .with(new EqualsAndHashCodeMatchRule())
            .with(new NoPublicFieldsExceptStaticFinalRule())
            .build();

        pojoValidator.validateRecursively("com.quorum.tessera.sync.model");

    }

}
