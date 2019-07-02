package com.quorum.tessera.config.constraints;

import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class PositiveIntegerValidatorTest {

    private PositiveIntegerValidator validator;

    private ConstraintValidatorContext context;

    @Before
    public void setUp() {
        this.validator = new PositiveIntegerValidator();
        this.context = mock(ConstraintValidatorContext.class);
    }

    @Test
    public void nullIsValid() {
        assertThat(validator.isValid(null, context)).isTrue();
    }

    @Test
    public void positiveIntegerIsValid() {
        assertThat(validator.isValid("10", context)).isTrue();
    }

    @Test
    public void zeroIsValid() {
        assertThat(validator.isValid("0", context)).isTrue();
    }

    @Test
    public void nonNumericCharactersAreInvalid() {
        assertThat(validator.isValid("letters", context)).isFalse();
    }

    @Test
    public void nonNumericCharactersFollowingNumbersIsInvalid() {
        assertThat(validator.isValid("10letters", context)).isFalse();
    }

    @Test
    public void decimalIsInvalid() {
        assertThat(validator.isValid("1.0", context)).isFalse();
    }

    @Test
    public void negativeIsInvalid() {
        assertThat(validator.isValid("-10", context)).isFalse();
    }
}
