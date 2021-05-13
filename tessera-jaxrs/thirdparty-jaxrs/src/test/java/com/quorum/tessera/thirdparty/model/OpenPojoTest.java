package com.quorum.tessera.thirdparty.model;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.rule.impl.NoPrimitivesRule;
import com.openpojo.validation.rule.impl.SetterMustExistRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

public class OpenPojoTest {

  @Test
  public void test() {

    final Validator validator =
        ValidatorBuilder.create()
            .with(new GetterMustExistRule())
            .with(new SetterMustExistRule())
            .with(new SetterTester())
            .with(new GetterTester())
            .with(new NoPrimitivesRule())
            .build();

    List<PojoClass> pojoClasses =
        Stream.of(GetPublicKeysResponse.class, Key.class)
            .map(PojoClassFactory::getPojoClass)
            .collect(Collectors.toList());

    validator.validate(pojoClasses);
  }
}
