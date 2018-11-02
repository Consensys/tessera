package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.DeprecatedServerConfig;
import java.util.Collections;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class EitherServerConfigsOrServerValidatorTest {

    private EitherServerConfigsOrServerValidator eitherServerConfigsOrServerValidator;

    private ConstraintValidatorContext constraintContext;

    @Before
    public void onSetUp() {
        eitherServerConfigsOrServerValidator = new EitherServerConfigsOrServerValidator();

        constraintContext = mock(ConstraintValidatorContext.class);
        ConstraintViolationBuilder constraintViolationBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(constraintContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(constraintViolationBuilder);

    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(constraintContext);
    }

    @Test
    public void ignoreNullArg() {
        ValidEitherServerConfigsOrServer validEitherServerConfigsOrServer = mock(ValidEitherServerConfigsOrServer.class);

        eitherServerConfigsOrServerValidator.initialize(validEitherServerConfigsOrServer);

        boolean outcome = eitherServerConfigsOrServerValidator.isValid(null, constraintContext);

        assertThat(outcome).isTrue();

    }

    @Test
    public void nullServerAndServerConfigs() {

        ValidEitherServerConfigsOrServer validEitherServerConfigsOrServer = mock(ValidEitherServerConfigsOrServer.class);

        eitherServerConfigsOrServerValidator.initialize(validEitherServerConfigsOrServer);

        Config config = new Config();

        boolean outcome = eitherServerConfigsOrServerValidator.isValid(config, constraintContext);

        assertThat(outcome).isFalse();
        
        verify(constraintContext).disableDefaultConstraintViolation();
        verify(constraintContext).buildConstraintViolationWithTemplate(anyString());

    }

    @Test
    public void cantHaveBoth() {

        ValidEitherServerConfigsOrServer validEitherServerConfigsOrServer = mock(ValidEitherServerConfigsOrServer.class);

        eitherServerConfigsOrServerValidator.initialize(validEitherServerConfigsOrServer);

        Config config = new Config();
        config.setServer(new DeprecatedServerConfig());
        config.setServerConfigs(Collections.emptyList());
        
        boolean outcome = eitherServerConfigsOrServerValidator.isValid(config, constraintContext);

        assertThat(outcome).isFalse();
        verify(constraintContext).disableDefaultConstraintViolation();
        verify(constraintContext).buildConstraintViolationWithTemplate(anyString());

    }
}
