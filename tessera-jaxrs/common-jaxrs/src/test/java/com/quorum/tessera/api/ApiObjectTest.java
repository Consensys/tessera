package com.quorum.tessera.api;

import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiObjectTest {

    @Test
    public void testAccessorsForApiObjects() {
        Validator validator = ValidatorBuilder.create().with(new GetterTester()).with(new SetterTester()).build();

        validator.validate(
                "com.quorum.tessera.api",
                pojoClass -> !pojoClass.getClazz().getName().contains(VersionTest.class.getName()));
    }

    @Test
    public void nonEmptyConstructor() {
        assertThat(new SendResponse("Data", new String[] {"arbitrary"}))
                .isNotNull()
                .extracting(SendResponse::getKey)
                .isNotNull();

        assertThat(new SendResponse("Data", new String[] {"arbitrary"}))
                .isNotNull()
                .extracting(SendResponse::getManagedParties)
                .isNotNull();

        assertThat(new StoreRawResponse("Data".getBytes()))
                .isNotNull()
                .extracting(StoreRawResponse::getKey)
                .isNotNull();
    }
}
