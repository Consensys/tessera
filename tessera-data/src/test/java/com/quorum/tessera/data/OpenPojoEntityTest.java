package com.quorum.tessera.data;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.*;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import com.quorum.tessera.data.staging.StagingTransaction;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class OpenPojoEntityTest {

  private PojoClass pojoClass;

  private Validator pojoValidator;

  public OpenPojoEntityTest(Map.Entry<Class<? extends Serializable>, Validator> typeValidatorPair) {
    this.pojoClass = PojoClassFactory.getPojoClass(typeValidatorPair.getKey());
    this.pojoValidator = typeValidatorPair.getValue();
  }

  @Test
  public void executeOpenPojoValidationsWithSetter() {
    pojoValidator.validate(pojoClass);
  }

  @Parameterized.Parameters(name = "{0}")
  public static Set<Map.Entry<Class<? extends Serializable>, Validator>> entities() {

    ValidatorBuilder validatorBuilder =
        ValidatorBuilder.create()
            .with(new GetterMustExistRule())
            .with(new SetterTester())
            .with(new GetterTester())
            .with(new EqualsAndHashCodeMatchRule())
            .with(new NoPublicFieldsExceptStaticFinalRule());

    Validator defaultValidator = validatorBuilder.build();

    return Map.of(
            MessageHash.class, validatorBuilder.with(new NoPrimitivesRule()).build(),
            EncryptedRawTransaction.class, defaultValidator,
            EncryptedTransaction.class, defaultValidator,
            StagingTransaction.class, defaultValidator)
        .entrySet();
  }
}
