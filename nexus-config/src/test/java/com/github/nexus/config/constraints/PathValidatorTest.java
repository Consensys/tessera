package com.github.nexus.config.constraints;

import org.junit.Test;

import javax.validation.ConstraintValidatorContext;
import java.nio.file.Path;
import java.nio.file.Paths;

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

}
