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
        UnsupportedKeyPair keyPair = new UnsupportedKeyPair(null, null, "public", null, null, null, null, null, null, null);

        validator.isValid(keyPair, context);

        verify(context).buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothDirectKeysRequired.message}");
    }

    @Test
    public void directViolationIfNoPublicKeyButPrivateKey() {
        UnsupportedKeyPair keyPair = new UnsupportedKeyPair(null, "private", null, null, null, null, null, null, null, null);

        validator.isValid(keyPair, context);

        verify(context).buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothDirectKeysRequired.message}");
    }

    @Test
    public void directViolationIsDefaultIfNoDirectPublicEvenIfMultipleIncompleteKeyPairTypesProvided() {
        KeyDataConfig keyDataConfig = mock(KeyDataConfig.class);
        Path path = mock(Path.class);

        UnsupportedKeyPair keyPair = new UnsupportedKeyPair(keyDataConfig, "private", null, path, null, null, "privVault", null, null, null);

        validator.isValid(keyPair, context);

        verify(context).buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothDirectKeysRequired.message}");
    }

    @Test
    public void directViolationIsDefaultIfNoDirectPrivateEvenIfMultipleIncompleteKeyPairTypesProvided() {
        KeyDataConfig keyDataConfig = mock(KeyDataConfig.class);
        Path path = mock(Path.class);

        UnsupportedKeyPair keyPair = new UnsupportedKeyPair(keyDataConfig, null, "public", null, path, "pubVault", null, null, null, null);

        validator.isValid(keyPair, context);

        verify(context).buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothDirectKeysRequired.message}");
    }

    @Test
    public void inlineViolationIfPrivateKeyConfigButNoPublicKey() {
        KeyDataConfig keyDataConfig = mock(KeyDataConfig.class);
        UnsupportedKeyPair keyPair = new UnsupportedKeyPair(keyDataConfig, null, null, null, null, null, null, null, null, null);

        validator.isValid(keyPair, context);

        verify(context).buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothInlineKeysRequired.message}");
    }

    @Test
    public void inlineViolationIfNoPublicEvenIfVaultAndFilesystemAreIncomplete() {
        KeyDataConfig keyDataConfig = mock(KeyDataConfig.class);
        Path path = mock(Path.class);

        UnsupportedKeyPair keyPair = new UnsupportedKeyPair(keyDataConfig, null, null, null, path, "pubId", null, null, null, null);

        validator.isValid(keyPair, context);

        verify(context).buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothInlineKeysRequired.message}");
    }

    @Test
    public void azureViolationIfPublicIdButNoPrivateId() {
        UnsupportedKeyPair keyPair = new UnsupportedKeyPair(null, null, null, null, null, "pubId", null, null, null, null);

        validator.isValid(keyPair, context);

        verify(context).buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothAzureKeysRequired.message}");
    }

    @Test
    public void azureViolationIfNoPublicIdButPrivateId() {
        UnsupportedKeyPair keyPair = new UnsupportedKeyPair(null, null, null, null, null, null, "privId", null, null, null);

        validator.isValid(keyPair, context);

        verify(context).buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothAzureKeysRequired.message}");
    }

    @Test
    public void azureViolationIfNoPublicIdEvenIfFilesystemIncomplete() {
        Path path = mock(Path.class);

        UnsupportedKeyPair keyPair = new UnsupportedKeyPair(null, null, null, null, path, null, "privId", null, null, null);

        validator.isValid(keyPair, context);

        verify(context).buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothAzureKeysRequired.message}");
    }

    @Test
    public void hashicorpViolationIfPublicIdButNoPrivateIdOrSecretPath() {
        UnsupportedKeyPair keyPair = new UnsupportedKeyPair(null, null, null, null, null, null, null, "pubId", null, null);

        validator.isValid(keyPair, context);

        verify(context).buildConstraintViolationWithTemplate("{UnsupportedKeyPair.allHashicorpKeyDataRequired.message}");
    }

    @Test
    public void hashicorpViolationIfPrivateIdButNoPublicIdOrSecretPath() {
        UnsupportedKeyPair keyPair = new UnsupportedKeyPair(null, null, null, null, null, null, null, null, "privId", null);

        validator.isValid(keyPair, context);

        verify(context).buildConstraintViolationWithTemplate("{UnsupportedKeyPair.allHashicorpKeyDataRequired.message}");
    }

    @Test
    public void hashicorpViolationIfSecretPathButNoPublicIdOrPrivateId() {
        UnsupportedKeyPair keyPair = new UnsupportedKeyPair(null, null, null, null, null, null, null, null, null, "secretPath");

        validator.isValid(keyPair, context);

        verify(context).buildConstraintViolationWithTemplate("{UnsupportedKeyPair.allHashicorpKeyDataRequired.message}");
    }

    @Test
    public void hashicorpViolationIfPublicIdAndPrivateIdButNoSecretPath() {
        UnsupportedKeyPair keyPair = new UnsupportedKeyPair(null, null, null, null, null, null, null, "pubId", "privId", null);

        validator.isValid(keyPair, context);

        verify(context).buildConstraintViolationWithTemplate("{UnsupportedKeyPair.allHashicorpKeyDataRequired.message}");
    }

    @Test
    public void hashicorpViolationIfPublicIdAndSecretPathButNoPrivateId() {
        UnsupportedKeyPair keyPair = new UnsupportedKeyPair(null, null, null, null, null, null, null, "pubId", null, "secretPath");

        validator.isValid(keyPair, context);

        verify(context).buildConstraintViolationWithTemplate("{UnsupportedKeyPair.allHashicorpKeyDataRequired.message}");
    }

    @Test
    public void hashicorpViolationIfPrivateIdAndSecretPathButNoPublicId() {
        UnsupportedKeyPair keyPair = new UnsupportedKeyPair(null, null, null, null, null, null, null, null, "privId", "secretPath");

        validator.isValid(keyPair, context);

        verify(context).buildConstraintViolationWithTemplate("{UnsupportedKeyPair.allHashicorpKeyDataRequired.message}");
    }



    @Test
    public void azureViolationIfNoPrivateIdEvenIfFilesystemIncomplete() {
        Path path = mock(Path.class);

        UnsupportedKeyPair keyPair = new UnsupportedKeyPair(null, null, null, null, path, "pubId", null, null, null, null);

        validator.isValid(keyPair, context);

        verify(context).buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothAzureKeysRequired.message}");
    }

    @Test
    public void filesystemViolationIfPublicPathButNoPrivatePath() {
        Path path = mock(Path.class);

        UnsupportedKeyPair keyPair = new UnsupportedKeyPair(null, null, null, null, path, null, null, null, null, null);

        validator.isValid(keyPair, context);

        verify(context).buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothFilesystemKeysRequired.message}");
    }

    @Test
    public void filesystemViolationIfNoPublicPathButPrivatePath() {
        Path path = mock(Path.class);

        UnsupportedKeyPair keyPair = new UnsupportedKeyPair(null, null, null, path, null, null, null, null, null, null);

        validator.isValid(keyPair, context);

        verify(context).buildConstraintViolationWithTemplate("{UnsupportedKeyPair.bothFilesystemKeysRequired.message}");
    }

    @Test
    public void defaultViolationIfNoRecognisedKeyPairDataProvided() {
        UnsupportedKeyPair keyPair = new UnsupportedKeyPair(null, null, null, null, null, null, null, null, null, null);

        validator.isValid(keyPair, context);

        verifyNoMoreInteractions(context);
    }

}
