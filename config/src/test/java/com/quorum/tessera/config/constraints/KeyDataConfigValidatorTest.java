package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.PrivateKeyType;
import javax.validation.ConstraintValidatorContext;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class KeyDataConfigValidatorTest {

    private ConstraintValidatorContext constraintValidatorContext;
    
    @Before
    public void onSetUp() {
        constraintValidatorContext = mock(ConstraintValidatorContext.class);
    }
    
    @After
    public void onTearDown() {
        verifyZeroInteractions(constraintValidatorContext);
    }
        
    
    @Test
    public void unlockedReturnsTrue() {


        KeyDataConfigValidator validator = new KeyDataConfigValidator();

        ValidKeyDataConfig validKeyDataConfig = mock(ValidKeyDataConfig.class);

        validator.initialize(validKeyDataConfig);

        KeyDataConfig keyDataConfig = mock(KeyDataConfig.class);
        when(keyDataConfig.getType()).thenReturn(PrivateKeyType.UNLOCKED);

        assertThat(validator.isValid(keyDataConfig, constraintValidatorContext)).isTrue();

    }

    @Test
    public void nullTypeReturnTrue() {


        KeyDataConfigValidator validator = new KeyDataConfigValidator();

        ValidKeyDataConfig validKeyDataConfig = mock(ValidKeyDataConfig.class);

        validator.initialize(validKeyDataConfig);

        KeyDataConfig keyDataConfig = mock(KeyDataConfig.class);

        assertThat(validator.isValid(keyDataConfig, constraintValidatorContext)).isTrue();

    }

    @Test
    public void nullConfigReturnsTrue() {

        KeyDataConfigValidator validator = new KeyDataConfigValidator();

        ValidKeyDataConfig validKeyDataConfig = mock(ValidKeyDataConfig.class);

        validator.initialize(validKeyDataConfig);

        assertThat(validator.isValid(null, constraintValidatorContext)).isTrue();

    }

    @Test
    public void lockedWithNullPasswordIsInvalid() {

        KeyDataConfigValidator validator = new KeyDataConfigValidator();

        ValidKeyDataConfig validKeyDataConfig = mock(ValidKeyDataConfig.class);
        validator.initialize(validKeyDataConfig);

        KeyDataConfig keyDataConfig = mock(KeyDataConfig.class);
        when(keyDataConfig.getType()).thenReturn(PrivateKeyType.LOCKED);

        assertThat(validator.isValid(keyDataConfig, constraintValidatorContext)).isFalse();

    }

    @Test
    public void lockedWithPasswordIsValid() {

        KeyDataConfigValidator validator = new KeyDataConfigValidator();

        ValidKeyDataConfig validKeyDataConfig = mock(ValidKeyDataConfig.class);
        validator.initialize(validKeyDataConfig);

        KeyDataConfig keyDataConfig = mock(KeyDataConfig.class);
        when(keyDataConfig.getType()).thenReturn(PrivateKeyType.LOCKED);
        when(keyDataConfig.getPassword()).thenReturn("SECRET");
        assertThat(validator.isValid(keyDataConfig, constraintValidatorContext)).isTrue();

    }
}
