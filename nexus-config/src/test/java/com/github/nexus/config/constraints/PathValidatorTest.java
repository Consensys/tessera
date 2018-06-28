package com.github.nexus.config.constraints;

import java.nio.file.Path;
import java.nio.file.Paths;
import javax.validation.ConstraintValidatorContext;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PathValidatorTest {

    public PathValidatorTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

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
