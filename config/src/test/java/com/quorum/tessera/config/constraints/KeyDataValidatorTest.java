package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.io.FilesDelegate;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import javax.validation.ConstraintValidatorContext;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class KeyDataValidatorTest {

    @Test
    public void validIgnoreNoFiles() {

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        ValidKeyData validKeyData = mock(ValidKeyData.class);

        KeyDataValidator validator = new KeyDataValidator();
        validator.initialize(validKeyData);

        KeyData keyData = mock(KeyData.class);

        assertThat(validator.isValid(Arrays.asList(keyData), context)).isTrue();

    }

    @Test
    public void invalidPrivateKeyIsNullButPublicIsPresent() {

        ValidKeyData validKeyData = mock(ValidKeyData.class);

        KeyDataValidator validator = new KeyDataValidator();
        validator.initialize(validKeyData);

        KeyData keyData = mock(KeyData.class);

        Path publicKeyPath = mock(Path.class);
        when(keyData.getPublicKeyPath()).thenReturn(publicKeyPath);

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        when(context.buildConstraintViolationWithTemplate(anyString()))
                .thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder.class));

        assertThat(validator.isValid(Arrays.asList(keyData), context)).isFalse();

        verify(context).buildConstraintViolationWithTemplate(anyString());
        verify(context).disableDefaultConstraintViolation();
        verifyNoMoreInteractions(context);
    }

    @Test
    public void invalidPrivateKeyNotExistsButPublicIsPresent() {

        ValidKeyData validKeyData = mock(ValidKeyData.class);

        KeyDataValidator validator = new KeyDataValidator();
        validator.initialize(validKeyData);

        KeyData keyData = mock(KeyData.class);

        Path publicKeyPath = mock(Path.class);
        when(keyData.getPublicKeyPath()).thenReturn(publicKeyPath);

        Path privateKeyPath = mock(Path.class);
        when(keyData.getPrivateKeyPath()).thenReturn(privateKeyPath);

        FilesDelegate filesDelegate = mock(FilesDelegate.class);
        when(filesDelegate.notExists(privateKeyPath)).thenReturn(true);
        when(filesDelegate.notExists(publicKeyPath)).thenReturn(false);

        validator.setFilesDelegate(filesDelegate);

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        when(context.buildConstraintViolationWithTemplate(anyString()))
                .thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder.class));

        assertThat(validator.isValid(Arrays.asList(keyData), context)).isFalse();

        verify(context).buildConstraintViolationWithTemplate(anyString());
        verify(context).disableDefaultConstraintViolation();
        verifyNoMoreInteractions(context);
    }

    @Test
    public void invalidPublicKeyNotExistsButPrivateIsPresent() {

        ValidKeyData validKeyData = mock(ValidKeyData.class);

        KeyDataValidator validator = new KeyDataValidator();
        validator.initialize(validKeyData);

        KeyData keyData = mock(KeyData.class);

        Path publicKeyPath = mock(Path.class);
        when(keyData.getPublicKeyPath()).thenReturn(publicKeyPath);

        Path privateKeyPath = mock(Path.class);
        when(keyData.getPrivateKeyPath()).thenReturn(privateKeyPath);

        FilesDelegate filesDelegate = mock(FilesDelegate.class);
        when(filesDelegate.notExists(privateKeyPath)).thenReturn(false);
        when(filesDelegate.notExists(publicKeyPath)).thenReturn(true);

        validator.setFilesDelegate(filesDelegate);

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        when(context.buildConstraintViolationWithTemplate(anyString()))
                .thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder.class));

        assertThat(validator.isValid(Arrays.asList(keyData), context)).isFalse();

        verify(context).buildConstraintViolationWithTemplate(anyString());
        verify(context).disableDefaultConstraintViolation();
        verifyNoMoreInteractions(context);
    }

    @Test
    public void allPathsPresentAndCorrect() {

        ValidKeyData validKeyData = mock(ValidKeyData.class);

        KeyDataValidator validator = new KeyDataValidator();
        validator.initialize(validKeyData);

        KeyData keyData = mock(KeyData.class);

        Path publicKeyPath = mock(Path.class);
        when(keyData.getPublicKeyPath()).thenReturn(publicKeyPath);

        Path privateKeyPath = mock(Path.class);
        when(keyData.getPrivateKeyPath()).thenReturn(privateKeyPath);

        FilesDelegate filesDelegate = mock(FilesDelegate.class);
        when(filesDelegate.notExists(privateKeyPath)).thenReturn(false);
        when(filesDelegate.notExists(publicKeyPath)).thenReturn(false);

        validator.setFilesDelegate(filesDelegate);

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        assertThat(validator.isValid(Arrays.asList(keyData), context)).isTrue();

        verifyNoMoreInteractions(context);
    }

    @Test
    public void empty() {

        ValidKeyData validKeyData = mock(ValidKeyData.class);

        KeyDataValidator validator = new KeyDataValidator();
        validator.initialize(validKeyData);

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        assertThat(validator.isValid(Collections.emptyList(), context)).isTrue();

        verifyNoMoreInteractions(context);
    }
    
        @Test
    public void nullList() {

        ValidKeyData validKeyData = mock(ValidKeyData.class);

        KeyDataValidator validator = new KeyDataValidator();
        validator.initialize(validKeyData);

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        assertThat(validator.isValid(null, context)).isTrue();

        verifyNoMoreInteractions(context);
    }
}
