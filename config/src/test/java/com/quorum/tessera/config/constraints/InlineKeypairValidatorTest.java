package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.PrivateKeyType;
import com.quorum.tessera.config.keypairs.InlineKeypair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class InlineKeypairValidatorTest {

    private InlineKeypairValidator validator = new InlineKeypairValidator();

    private ValidInlineKeypair validInlineKeypair = mock(ValidInlineKeypair.class);

    private ConstraintValidatorContext constraintValidatorContext;
    
    @Before
    public void onSetUp() {
        this.constraintValidatorContext = mock(ConstraintValidatorContext.class);
        this.validator.initialize(validInlineKeypair);
    }
    
    @After
    public void onTearDown() {
        verifyZeroInteractions(constraintValidatorContext);
    }
        
    
    @Test
    public void unlockedReturnsTrue() {

        InlineKeypair keyDataConfig = mock(InlineKeypair.class);
        KeyDataConfig keyData = mock(KeyDataConfig.class);
        when(keyDataConfig.getPrivateKeyConfig()).thenReturn(keyData);
        when(keyDataConfig.getPrivateKeyConfig().getType()).thenReturn(PrivateKeyType.UNLOCKED);

        assertThat(validator.isValid(keyDataConfig, constraintValidatorContext)).isTrue();

    }

    @Test
    public void nullTypeReturnTrue() {

        InlineKeypair keyDataConfig = mock(InlineKeypair.class);
        KeyDataConfig keyData = mock(KeyDataConfig.class);
        when(keyDataConfig.getPrivateKeyConfig()).thenReturn(keyData);

        assertThat(validator.isValid(keyDataConfig, constraintValidatorContext)).isTrue();

    }

    @Test
    public void nullConfigReturnsTrue() {

        assertThat(validator.isValid(null, constraintValidatorContext)).isTrue();

    }

    @Test
    public void lockedWithNullPasswordIsInvalid() {

        InlineKeypair keyDataConfig = mock(InlineKeypair.class);
        KeyDataConfig keyData = mock(KeyDataConfig.class);
        when(keyDataConfig.getPrivateKeyConfig()).thenReturn(keyData);
        when(keyDataConfig.getPassword()).thenReturn("");
        when(keyDataConfig.getPrivateKeyConfig().getType()).thenReturn(PrivateKeyType.LOCKED);

        assertThat(validator.isValid(keyDataConfig, constraintValidatorContext)).isFalse();

    }

    @Test
    public void lockedWithPasswordIsValid() {

        InlineKeypair keyDataConfig = mock(InlineKeypair.class);
        KeyDataConfig keyData = mock(KeyDataConfig.class);
        when(keyDataConfig.getPrivateKeyConfig()).thenReturn(keyData);
        when(keyDataConfig.getPrivateKeyConfig().getType()).thenReturn(PrivateKeyType.LOCKED);
        when(keyDataConfig.getPassword()).thenReturn("SECRET");
        assertThat(validator.isValid(keyDataConfig, constraintValidatorContext)).isTrue();

    }
}
