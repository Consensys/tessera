package com.quorum.tessera.config;

import com.quorum.tessera.config.util.FilesDelegate;
import com.quorum.tessera.config.util.MockFilesDelegate;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ValidationTest {

    private final Validator validator = Validation
            .buildDefaultValidatorFactory().getValidator();

    public ValidationTest() {
    }

    @Test
    public void validateArgonOptions() {
        ArgonOptions options = new ArgonOptions("d", 10, 20, 30);

        Set<ConstraintViolation<ArgonOptions>> violations = validator.validate(options);

        assertThat(violations).isEmpty();

    }

    @Test
    public void validateArgonOptionsInvalidAlgo() {
        ArgonOptions options = new ArgonOptions("a", 10, 20, 30);

        Set<ConstraintViolation<ArgonOptions>> violations = validator.validate(options);

        assertThat(violations).hasSize(1);

    }

    @Test
    public void validateArgonOptionsAllNullAlgoHasDefaultValue() {
        ArgonOptions options = new ArgonOptions(null, null, null, null);

        Set<ConstraintViolation<ArgonOptions>> violations = validator.validate(options);

        assertThat(violations).hasSize(3);
        assertThat(options.getAlgorithm()).isEqualTo("id");

    }

    @Test
    public void validUnixDomainPath() {

        Path unixSocketFile = mock(Path.class);

        FilesDelegate filesDelegate = MockFilesDelegate.setUpMock();
        when(filesDelegate.notExists(unixSocketFile)).thenReturn(true);

        Config config = new Config(null, null, null, null, null, unixSocketFile, true);

        Set<ConstraintViolation<Config>> violations = validator.validateProperty(config, "unixSocketFile");

        assertThat(violations).isEmpty();

        verify(filesDelegate).createFile(unixSocketFile);
        verify(filesDelegate).deleteIfExists(unixSocketFile);
        verify(filesDelegate).notExists(unixSocketFile);
        verifyNoMoreInteractions(filesDelegate);
        MockFilesDelegate.tearDownMock();

    }

    @Test
    public void invalidCannotCreateUnixDomainPath() {

        Path unixSocketFile = mock(Path.class);

        FilesDelegate filesDelegate = MockFilesDelegate.setUpMock();
        when(filesDelegate.notExists(unixSocketFile)).thenReturn(true);
        when(filesDelegate.createFile(unixSocketFile)).thenThrow(UncheckedIOException.class);

        Config config = new Config(null, null, null, null, null, unixSocketFile, true);

        Set<ConstraintViolation<Config>> violations = validator.validateProperty(config, "unixSocketFile");

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("unixSocketFile");

        verify(filesDelegate).createFile(unixSocketFile);
        verify(filesDelegate).deleteIfExists(unixSocketFile);
        verify(filesDelegate).notExists(unixSocketFile);
        verifyNoMoreInteractions(filesDelegate);
        MockFilesDelegate.tearDownMock();

    }

}
