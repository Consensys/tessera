package com.quorum.tessera.encryption;

import static nl.jqno.equalsverifier.Warning.STRICT_INHERITANCE;
import static org.assertj.core.api.Assertions.assertThat;

import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.test.impl.GetterTester;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class NonceTest {

  @Test
  public void pojo() {
    EqualsVerifier.configure().suppress(STRICT_INHERITANCE).forClass(Nonce.class).verify();

    ValidatorBuilder.create()
        .with(new GetterTester())
        .build()
        .validate(PojoClassFactory.getPojoClass(Nonce.class));
  }

  @Test
  public void toStringDoesNotUseUnderlyingData() {
    final Nonce nonce = new Nonce(new byte[] {5, 6, 7});
    final Nonce nonce2 = new Nonce(new byte[] {5, 6, 7});

    assertThat(nonce.toString()).isNotEqualTo(nonce2.toString());
  }
}
