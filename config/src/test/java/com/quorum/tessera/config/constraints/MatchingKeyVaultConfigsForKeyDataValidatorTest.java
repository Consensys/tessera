package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.KeyData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintValidatorContext;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class MatchingKeyVaultConfigsForKeyDataValidatorTest {

    private MatchingKeyVaultConfigsForKeyDataValidator validator;

    private MatchingKeyVaultConfigsForKeyData annotation;

    private ConstraintValidatorContext constraintValidatorContext;

    @Before
    public void onSetup() {

        constraintValidatorContext = mock(ConstraintValidatorContext.class);

        annotation = mock(MatchingKeyVaultConfigsForKeyData.class);

        validator = new MatchingKeyVaultConfigsForKeyDataValidator();
        validator.initialize(annotation);

    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(annotation,constraintValidatorContext);

    }

    @Test
    public void isvalid() {


        KeyConfiguration keyConfiguration = new KeyConfiguration();
        KeyData keyData = new KeyData();
        keyConfiguration.setKeyData(Collections.singletonList(keyData));

        boolean result = validator.isValid(keyConfiguration,constraintValidatorContext);

        assertThat(result).isTrue();

    }



}
