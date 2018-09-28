package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.keypairs.UnsupportedKeyPair;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintValidatorContext;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UnsupportedKeyPairValidatorTest {
    private UnsupportedKeyPairValidator validator;
    private ValidUnsupportedKeyPair validUnsupportedKeyPair;
    private ConstraintValidatorContext context;

    @Before
    public void setUp() {
        this.validator = new UnsupportedKeyPairValidator();
        this.validUnsupportedKeyPair = mock(ValidUnsupportedKeyPair.class);

        validator.initialize(validUnsupportedKeyPair);

        this.context = mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);

        when(context.buildConstraintViolationWithTemplate(any(String.class))).thenReturn(builder);
    }

    @Test
    public void directViolationIfPublicKeyButNoPrivateKey() {
        UnsupportedKeyPair keyPair = new UnsupportedKeyPair(null, null, "public", null, null);

        validator.isValid(keyPair, context);

        verify(context).buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothDirectKeysRequired.message}");
    }

    @Test
    public void directViolationIfNoPublicKeyButPrivateKey() {
        UnsupportedKeyPair keyPair = new UnsupportedKeyPair(null, "private", null, null, null);

        validator.isValid(keyPair, context);

        verify(context).buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothDirectKeysRequired.message}");
    }

    @Test
    public void directViolationIsDefaultIfNoDirectPublicEvenIfMultipleIncompleteKeyPairTypesProvided() {
        KeyDataConfig keyDataConfig = mock(KeyDataConfig.class);
        Path path = mock(Path.class);

        UnsupportedKeyPair keyPair = new UnsupportedKeyPair(keyDataConfig, "private", null, path, null);

        validator.isValid(keyPair, context);

        verify(context).buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothDirectKeysRequired.message}");
    }

    @Test
    public void directViolationIsDefaultIfNoDirectPrivateEvenIfMultipleIncompleteKeyPairTypesProvided() {
        KeyDataConfig keyDataConfig = mock(KeyDataConfig.class);
        Path path = mock(Path.class);

        UnsupportedKeyPair keyPair = new UnsupportedKeyPair(keyDataConfig, null, "public", null, path);

        validator.isValid(keyPair, context);

        verify(context).buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothDirectKeysRequired.message}");
    }

    @Test
    public void inlineViolationIfPrivateKeyConfigButNoPublicKey() {
        KeyDataConfig keyDataConfig = mock(KeyDataConfig.class);
        UnsupportedKeyPair keyPair = new UnsupportedKeyPair(keyDataConfig, null, null, null, null);

        validator.isValid(keyPair, context);

        verify(context).buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothInlineKeysRequired.message}");
    }

    @Test
    public void inlineViolationIfNoPublicEvenIfFilesystemIsIncomplete() {
        KeyDataConfig keyDataConfig = mock(KeyDataConfig.class);
        Path path = mock(Path.class);

        UnsupportedKeyPair keyPair = new UnsupportedKeyPair(keyDataConfig, null, null, null, path);

        validator.isValid(keyPair, context);

        verify(context).buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothInlineKeysRequired.message}");
    }

    @Test
    public void filesystemViolationIfPublicPathButNoPrivatePath() {
        Path path = mock(Path.class);

        UnsupportedKeyPair keyPair = new UnsupportedKeyPair(null, null, null, null, path);

        validator.isValid(keyPair, context);

        verify(context).buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothFilesystemKeysRequired.message}");
    }

    @Test
    public void filesystemViolationIfNoPublicPathButPrivatePath() {
        Path path = mock(Path.class);

        UnsupportedKeyPair keyPair = new UnsupportedKeyPair(null, null, null, path, null);

        validator.isValid(keyPair, context);

        verify(context).buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothFilesystemKeysRequired.message}");
    }

    @Test
    public void defaultViolationIfNoRecognisedKeyPairDataProvided() {
        UnsupportedKeyPair keyPair = new UnsupportedKeyPair(null, null, null, null, null);

        validator.isValid(keyPair, context);

        verifyNoMoreInteractions(context);
    }

}
