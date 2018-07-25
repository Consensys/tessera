package com.quorum.tessera.config.constraints;

import java.io.IOException;
import java.nio.file.Files;
import org.junit.Test;

import javax.validation.ConstraintValidatorContext;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PathValidatorTest {

    @Test
    public void validateFileExists() {

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        ValidPath validPath = mock(ValidPath.class);
        when(validPath.checkExists()).thenReturn(true);

        PathValidator pathValidator = new PathValidator();
        pathValidator.initialize(validPath);

        Path path = Paths.get("bogus");

        assertThat(pathValidator.isValid(path, context)).isFalse();

    }

    @Test
    public void validateFileExistsDontCheck() {

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        ValidPath validPath = mock(ValidPath.class);
        when(validPath.checkExists()).thenReturn(false);

        PathValidator pathValidator = new PathValidator();
        pathValidator.initialize(validPath);

        Path path = Paths.get("bogus");

        assertThat(pathValidator.isValid(path, context)).isTrue();

    }

    @Test
    public void validateFileExistsWhenFileDoesExist() throws IOException {

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        ValidPath validPath = mock(ValidPath.class);
        when(validPath.checkExists()).thenReturn(true);

        PathValidator pathValidator = new PathValidator();
        pathValidator.initialize(validPath);

        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path actualFile = Files.createTempFile(tempDir, UUID.randomUUID().toString(), ".txt");

        assertThat(pathValidator.isValid(actualFile, context)).isTrue();

        Files.deleteIfExists(actualFile);

    }
}
