package com.quorum.tessera.ssl.context.model;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.EqualsAndHashCodeMatchRule;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.rule.impl.NoPublicFieldsExceptStaticFinalRule;
import com.openpojo.validation.test.impl.GetterTester;
import org.junit.Test;

public class SSLContextPropertiesTest {

  @Test
  public void executeOpenPojoValidationsNoSetter() {

    PojoClass pojoClass = PojoClassFactory.getPojoClass(SSLContextProperties.class);

    final Validator pojoValidator =
        ValidatorBuilder.create()
            .with(new GetterMustExistRule())
            .with(new GetterTester())
            .with(new EqualsAndHashCodeMatchRule())
            .with(new NoPublicFieldsExceptStaticFinalRule())
            .build();

    pojoValidator.validate(pojoClass);
  }
}
