package com.quorum.tessera.enclave;

import com.openpojo.reflection.filters.FilterClassName;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.EqualsAndHashCodeMatchRule;
import com.openpojo.validation.test.impl.GetterTester;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import static nl.jqno.equalsverifier.Warning.STRICT_INHERITANCE;

public class PojoTest {

    @Test
    public void openPojoTests() {

        final Validator pojoValidator = ValidatorBuilder.create()
            .with(new GetterTester())
            .with(new EqualsAndHashCodeMatchRule())
            .build();

        pojoValidator.validate(RawTransaction.class.getPackage().getName(), new FilterClassName("RawTransaction"));
        pojoValidator.validate(EncodedPayload.class.getPackage().getName(), new FilterClassName("EncodedPayload"));
    }

    @Test
    public void equalsAndHashcode() {
        EqualsVerifier.configure().suppress(STRICT_INHERITANCE).forClass(RawTransaction.class).verify();
    }

}
