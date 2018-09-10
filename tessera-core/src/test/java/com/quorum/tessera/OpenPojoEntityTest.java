package com.quorum.tessera;

import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.*;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import org.junit.Test;

public class OpenPojoEntityTest {

    @Test
    public void executeOpenPojoValidationsWithSetter() {

        final Validator pojoValidator = ValidatorBuilder.create()
                .with(new GetterMustExistRule())
                .with(new SetterMustExistRule())
                .with(new SetterTester())
                .with(new GetterTester())
                .with(new EqualsAndHashCodeMatchRule())
                .with(new NoPrimitivesRule())
                .with(new NoPublicFieldsExceptStaticFinalRule())
                .build();

        pojoValidator.validate("com.quorum.tessera.api.model");
        pojoValidator.validateRecursively("com.quorum.tessera.enclave.model");
    }


}
