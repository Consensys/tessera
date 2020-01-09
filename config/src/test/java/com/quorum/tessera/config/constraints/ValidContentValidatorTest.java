package com.quorum.tessera.config.constraints;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;
import javax.validation.ConstraintValidatorContext;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ValidContentValidatorTest {

    @Test
    public void ignoreNullPath() {
        ValidContentValidator validator = new ValidContentValidator();
        ValidContent validContent = mock(ValidContent.class);
        validator.initialize(validContent);

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        assertThat(validator.isValid(null, context)).isTrue();
    }

    @Test
    public void ignoreNonExistPath() {
        ValidContentValidator validator = new ValidContentValidator();
        ValidContent validContent = mock(ValidContent.class);
        validator.initialize(validContent);

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        Path path = Paths.get(UUID.randomUUID().toString());

        assertThat(path).doesNotExist();

        assertThat(validator.isValid(path, context)).isTrue();
    }

    @Test
    public void defaultValuesIgnoreEmptyFile() throws Exception {
        ValidContentValidator validator = new ValidContentValidator();
        ValidContent validContent = mock(ValidContent.class);

        validator.initialize(validContent);

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        Path path = Files.createTempFile(UUID.randomUUID().toString(), "");

        assertThat(path).exists();

        assertThat(validator.isValid(path, context)).isTrue();
    }

    @Test
    public void expectSingleLineButFileIsEmpty() throws Exception {
        ValidContentValidator validator = new ValidContentValidator();
        ValidContent validContent = mock(ValidContent.class);
        when(validContent.minLines()).thenReturn(1);
        when(validContent.maxLines()).thenReturn(1);

        validator.initialize(validContent);

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        Path path = Files.createTempFile(UUID.randomUUID().toString(), "");

        assertThat(path).exists();

        assertThat(validator.isValid(path, context)).isFalse();
    }

    @Test
    public void expectSingleLineFileIsValid() throws Exception {
        ValidContentValidator validator = new ValidContentValidator();
        ValidContent validContent = mock(ValidContent.class);
        when(validContent.minLines()).thenReturn(1);
        when(validContent.maxLines()).thenReturn(1);

        validator.initialize(validContent);

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        Path path = Files.createTempFile(UUID.randomUUID().toString(), "");
        Files.write(path, "SOMEDATA".getBytes());

        assertThat(path).exists();

        assertThat(validator.isValid(path, context)).isTrue();
    }

    @Test
    public void tooManyLines() throws Exception {
        ValidContentValidator validator = new ValidContentValidator();
        ValidContent validContent = mock(ValidContent.class);
        when(validContent.minLines()).thenReturn(1);
        when(validContent.maxLines()).thenReturn(1);

        validator.initialize(validContent);

        final ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        Path path = Files.createTempFile(UUID.randomUUID().toString(), "");
        Files.write(path, Arrays.asList("SOMEDATA", "SOMEMOREDATA"));

        assertThat(path).exists();

        assertThat(validator.isValid(path, context)).isFalse();
    }

    @Test
    public void emptyLine() throws Exception {
        ValidContentValidator validator = new ValidContentValidator();
        ValidContent validContent = mock(ValidContent.class);
        when(validContent.minLines()).thenReturn(1);
        when(validContent.maxLines()).thenReturn(1);

        validator.initialize(validContent);

        final ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        Path path = Files.createTempFile(UUID.randomUUID().toString(), "");
        Files.write(path, Arrays.asList(""));

        assertThat(path).exists();

        assertThat(validator.isValid(path, context)).isFalse();
    }
}
