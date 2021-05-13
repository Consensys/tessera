package com.quorum.tessera.enclave;

import static nl.jqno.equalsverifier.Warning.STRICT_INHERITANCE;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.EqualsAndHashCodeMatchRule;
import com.openpojo.validation.test.impl.GetterTester;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class RawTransactionTest {

  @Test
  public void openPojoTests() {

    final Validator pojoValidator =
        ValidatorBuilder.create()
            .with(new GetterTester())
            .with(new EqualsAndHashCodeMatchRule())
            .build();

    PojoClass pojoClass = PojoClassFactory.getPojoClass(RawTransaction.class);

    pojoValidator.validate(pojoClass);
  }

  @Test
  public void equalsAndHashcode() {
    EqualsVerifier.configure().suppress(STRICT_INHERITANCE).forClass(RawTransaction.class).verify();
  }
}
