package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.DeprecatedServerConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class EitherServerConfigsOrServerValidatorTest {

    private EitherServerConfigsOrServerValidator eitherServerConfigsOrServerValidator;

    private ConstraintValidatorContext constraintContext;

    @Before
    public void onSetUp() {
        eitherServerConfigsOrServerValidator = new EitherServerConfigsOrServerValidator();

        constraintContext = mock(ConstraintValidatorContext.class);
        ConstraintViolationBuilder constraintViolationBuilder =
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(constraintContext.buildConstraintViolationWithTemplate(anyString()))
                .thenReturn(constraintViolationBuilder);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(constraintContext);
    }

    @Test
    public void ignoreNullArg() {
        boolean outcome = eitherServerConfigsOrServerValidator.isValid(null, constraintContext);

        assertThat(outcome).isTrue();
    }

    @Test
    public void nullServerAndServerConfigs() {
        Config config = new Config();

        boolean outcome = eitherServerConfigsOrServerValidator.isValid(config, constraintContext);

        assertThat(outcome).isFalse();

        verify(constraintContext).disableDefaultConstraintViolation();
        verify(constraintContext).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    public void cantHaveBoth() {
        Config config = new Config();
        config.setServer(new DeprecatedServerConfig());
        config.setServerConfigs(Collections.emptyList());

        boolean outcome = eitherServerConfigsOrServerValidator.isValid(config, constraintContext);

        assertThat(outcome).isFalse();
        verify(constraintContext).disableDefaultConstraintViolation();
        verify(constraintContext).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    public void unixFileRequiredWhenDeprecatedServer() {
        Config config = new Config();
        config.setServer(new DeprecatedServerConfig());
        config.setUnixSocketFile(null);

        boolean outcome = eitherServerConfigsOrServerValidator.isValid(config, constraintContext);

        assertThat(outcome).isFalse();
        verify(constraintContext).disableDefaultConstraintViolation();
        verify(constraintContext).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    public void validConfigReturnsTrue() throws IOException {
        final Path socketPath = Files.createTempFile("socket", ".ipc");
        Config config = new Config();
        config.setServer(new DeprecatedServerConfig());
        config.setUnixSocketFile(socketPath);

        boolean outcome = eitherServerConfigsOrServerValidator.isValid(config, constraintContext);

        assertThat(outcome).isTrue();
    }
}
