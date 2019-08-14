package com.quorum.tessera.partyinfo;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.rule.impl.SetterMustExistRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import org.junit.Test;

public class ResendRequestTest {

    public ResendRequestTest() {}

    @Test
    public void executeOpenPojoValidations() {

        Validator pojoValidator =
                ValidatorBuilder.create()
                        .with(new GetterMustExistRule())
                        .with(new SetterMustExistRule())
                        .with(new SetterTester())
                        .with(new GetterTester())
                        .build();

        PojoClass resendRequest = PojoClassFactory.getPojoClass(ResendRequest.class);
        pojoValidator.validate(resendRequest);
    }
}
