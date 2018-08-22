package com.quorum.tessera.config.constraints;

import java.util.Base64;
import java.util.UUID;
import javax.validation.ConstraintValidatorContext;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

public class Base64ValidatorTest {

    private Base64Validator validator;

    private ConstraintValidatorContext constraintValidatorContext;

    private ValidBase64 annotation;

    @Before
    public void onSetup() {
        validator = new Base64Validator();

        constraintValidatorContext = mock(ConstraintValidatorContext.class);

        annotation = mock(ValidBase64.class);

        validator.initialize(annotation);

    }

    @After
    public void onTearDown() {
        verifyZeroInteractions(constraintValidatorContext, annotation);
    }

    @Test
    public void validBase64() {

        String value = Base64.getEncoder().encodeToString("HELLOW".getBytes());

        assertThat(validator.isValid(value, constraintValidatorContext))
                .isTrue();

    }

    @Test
    public void invalidBase64() {

        String value = UUID.randomUUID().toString();

        assertThat(validator.isValid(value, constraintValidatorContext))
                .isFalse();

    }

    @Test
    public void nullValueIsIgnoredAndReturns() {

        assertThat(validator.isValid(null, constraintValidatorContext))
                .isTrue();

    }

    @Test
    public void naclFailureValueIsIgnoredAndReturns() {

        assertThat(validator.isValid("NACL_FAILURE: It's broken son!!", constraintValidatorContext))
                .isTrue();
        assertThat(validator.isValid("NACL_FAILURE", constraintValidatorContext))
                .isTrue();

    }

}
