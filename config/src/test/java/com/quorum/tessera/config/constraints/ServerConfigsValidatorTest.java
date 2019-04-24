package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.ServerConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ServerConfigsValidatorTest {

    private List<ServerConfig> serverConfigs;

    private ServerConfig p2pServerConfig;

    private ServerConfig q2tServerConfig;

    private ServerConfig thirdPartyServerConfig;

    private ConstraintValidatorContext cvc;

    private ServerConfigsValidator validator;

    @Before
    public void onSetUp() {
        cvc = mock(ConstraintValidatorContext.class);
        p2pServerConfig = new ServerConfig();
        p2pServerConfig.setApp(AppType.P2P);
        p2pServerConfig.setEnabled(true);
        p2pServerConfig.setServerAddress("localhost:123");
        p2pServerConfig.setCommunicationType(CommunicationType.REST);
        p2pServerConfig.setSslConfig(null);
        p2pServerConfig.setInfluxConfig(null);
        p2pServerConfig.setBindingAddress(null);

        q2tServerConfig = new ServerConfig(AppType.Q2T, true,
            "localhost:1234", CommunicationType.REST,
            null, null, null, null);
        thirdPartyServerConfig = new ServerConfig(AppType.THIRD_PARTY, true,
            "localhost:12345", CommunicationType.REST,
            null, null, null, null);

        serverConfigs = new ArrayList<>(Arrays.asList(p2pServerConfig, q2tServerConfig, thirdPartyServerConfig));

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
        thirdPartyServerConfig.setEnabled(false);
        assertThat(validator.isValid(serverConfigs, cvc)).isTrue();
    }

    @Test
    public void isNotValidWhenNoP2PServersAreEnabled() {
        p2pServerConfig.setEnabled(false);
        assertThat(validator.isValid(serverConfigs, cvc)).isFalse();
        verify(cvc).disableDefaultConstraintViolation();
        verify(cvc).buildConstraintViolationWithTemplate(eq("Only one P2P server must be configured and enabled."));
    }

    @Test
    public void isNotValidWhenNoP2PServersAreDefined() {
        serverConfigs.remove(p2pServerConfig);
        assertThat(validator.isValid(serverConfigs, cvc)).isFalse();
        verify(cvc).disableDefaultConstraintViolation();
        verify(cvc).buildConstraintViolationWithTemplate(eq("Only one P2P server must be configured and enabled."));
    }

    @Test
    public void isNotValidWhenTwoOrMoreP2PServersAreDefinedAndEnabled() {
        serverConfigs.add(p2pServerConfig);
        assertThat(validator.isValid(serverConfigs, cvc)).isFalse();
        verify(cvc).disableDefaultConstraintViolation();
        verify(cvc).buildConstraintViolationWithTemplate(eq("Only one P2P server must be configured and enabled."));
    }

    @Test
    public void isNotValidWhenNoQ2TServersAreEnabled() {
        q2tServerConfig.setEnabled(false);
        assertThat(validator.isValid(serverConfigs, cvc)).isFalse();
        verify(cvc).disableDefaultConstraintViolation();
        verify(cvc).buildConstraintViolationWithTemplate(eq("At least one Q2T server must be configured and enabled."));
    }

    @Test
    public void isNotValidWhenNoQ2TServersAreDefined() {
        serverConfigs.remove(q2tServerConfig);
        assertThat(validator.isValid(serverConfigs, cvc)).isFalse();
        verify(cvc).disableDefaultConstraintViolation();
        verify(cvc).buildConstraintViolationWithTemplate(eq("At least one Q2T server must be configured and enabled."));
    }
}
