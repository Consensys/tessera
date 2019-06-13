package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.ServerConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.quorum.tessera.config.CommunicationType.REST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ServerConfigsValidatorTest {

    private List<ServerConfig> serverConfigs;

    private ServerConfig p2pServerConfig;

    private ConstraintValidatorContext cvc;

    private ServerConfigsValidator validator;

    @Before
    public void onSetUp() {
        this.cvc = mock(ConstraintValidatorContext.class);

        p2pServerConfig = new ServerConfig(AppType.P2P, true, "localhost:123", REST, null, null, null);

        serverConfigs = new ArrayList<>(Collections.singletonList(p2pServerConfig));

        validator = new ServerConfigsValidator();

        when(cvc.buildConstraintViolationWithTemplate(anyString()))
            .thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder.class));
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(cvc);
    }

    @Test
    public void isValidWhenServerConfigsIsNull() {
        assertThat(validator.isValid(null, cvc)).isTrue();
    }

    @Test
    public void isValidWhenValidDataIsSupplied() {
        assertThat(validator.isValid(serverConfigs, cvc)).isTrue();
    }

    @Test
    public void isNotValidWhenNoP2PServersAreEnabled() {
        p2pServerConfig.setEnabled(false);

        assertThat(validator.isValid(serverConfigs, cvc)).isFalse();
        verify(cvc).disableDefaultConstraintViolation();
        verify(cvc).buildConstraintViolationWithTemplate(eq("Exactly one P2P server must be configured and enabled."));
    }

    @Test
    public void isNotValidWhenNoP2PServersAreDefined() {
        serverConfigs.remove(p2pServerConfig);

        assertThat(validator.isValid(serverConfigs, cvc)).isFalse();
        verify(cvc).disableDefaultConstraintViolation();
        verify(cvc).buildConstraintViolationWithTemplate(eq("Exactly one P2P server must be configured and enabled."));
    }

    @Test
    public void isNotValidWhenTwoOrMoreP2PServersAreDefinedAndEnabled() {
        serverConfigs.add(p2pServerConfig);

        assertThat(validator.isValid(serverConfigs, cvc)).isFalse();
        verify(cvc).disableDefaultConstraintViolation();
        verify(cvc).buildConstraintViolationWithTemplate(eq("Exactly one P2P server must be configured and enabled."));
    }

}
