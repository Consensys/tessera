package com.quorum.tessera.nacl;

import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.test.impl.GetterTester;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import static nl.jqno.equalsverifier.Warning.STRICT_INHERITANCE;
import static org.assertj.core.api.Assertions.assertThat;

public class NonceTest {

    @Test
    public void pojo() {
        EqualsVerifier.configure().suppress(STRICT_INHERITANCE).forClass(Nonce.class).verify();

        ValidatorBuilder.create().with(new GetterTester()).build().validate(PojoClassFactory.getPojoClass(Nonce.class));
    }

    @Test
    public void toStringGivesCorrectOutput() {
        final Nonce nonce = new Nonce(new byte[] {5, 6, 7});

        final String toString = nonce.toString();

        assertThat(toString).isEqualTo("[5, 6, 7]");
    }
}
