package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.io.FilesDelegate;
import java.nio.file.Path;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderDefinedContext;
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

        assertThat(validator.isValid(keyData, context)).isTrue();

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

        assertThat(validator.isValid(keyData, context)).isFalse();

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
        ConstraintViolationBuilder constraintViolationBuilder = mock(ConstraintViolationBuilder.class);

        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(constraintViolationBuilder);
        NodeBuilderDefinedContext nodeBuilderDefinedContext = mock(NodeBuilderDefinedContext.class);
        when(constraintViolationBuilder.addNode(anyString())).thenReturn(nodeBuilderDefinedContext);

        assertThat(validator.isValid(keyData, context)).isFalse();

        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(anyString());

        verify(constraintViolationBuilder).addNode(anyString());
        verify(nodeBuilderDefinedContext).addConstraintViolation();

        verifyNoMoreInteractions(context, constraintViolationBuilder, nodeBuilderDefinedContext);
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

        ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilder
                = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);

        NodeBuilderDefinedContext nodeBuilderDefinedContext = mock(NodeBuilderDefinedContext.class);
        when(constraintViolationBuilder.addNode(anyString())).thenReturn(nodeBuilderDefinedContext);

        when(context.buildConstraintViolationWithTemplate(anyString()))
                .thenReturn(constraintViolationBuilder);

        assertThat(validator.isValid(keyData, context)).isFalse();

        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(anyString());
        verify(constraintViolationBuilder).addNode(anyString());
        verify(nodeBuilderDefinedContext).addConstraintViolation();

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

        assertThat(validator.isValid(keyData, context)).isTrue();

        verifyNoMoreInteractions(context);
    }

    @Test
    public void nullKeyData() {

        ValidKeyData validKeyData = mock(ValidKeyData.class);

        KeyDataValidator validator = new KeyDataValidator();
        validator.initialize(validKeyData);

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        assertThat(validator.isValid(null, context)).isTrue();

        verifyNoMoreInteractions(context);
    }

    @Test
    public void invalidPublicKeyIsNullButPrivateIsPresent() {

        ValidKeyData validKeyData = mock(ValidKeyData.class);

        KeyDataValidator validator = new KeyDataValidator();
        validator.initialize(validKeyData);

        KeyData keyData = mock(KeyData.class);

        Path privateKeyPath = mock(Path.class);
        when(keyData.getPrivateKeyPath()).thenReturn(privateKeyPath);

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        when(context.buildConstraintViolationWithTemplate(anyString()))
                .thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder.class));

        assertThat(validator.isValid(keyData, context)).isFalse();

        verify(context).buildConstraintViolationWithTemplate(anyString());
        verify(context).disableDefaultConstraintViolation();
        verifyNoMoreInteractions(context);
    }
}
