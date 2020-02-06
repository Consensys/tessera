package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.ServerConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ServerConfigsValidatorTest {

    private Map<AppType, ServerConfig> serverConfigs = new HashMap<>();

    private ConstraintValidatorContext cvc;

    private ServerConfigsValidator validator;

    @Before
    public void onSetUp() {
        cvc = mock(ConstraintValidatorContext.class);

        for (AppType appType : AppType.values()) {

            ServerConfig serverConfig = new ServerConfig();
            serverConfig.setApp(appType);
            serverConfig.setServerAddress("localhost:123");
            serverConfig.setCommunicationType(CommunicationType.REST);
            serverConfig.setSslConfig(null);
            serverConfig.setInfluxConfig(null);
            serverConfig.setBindingAddress(null);

            serverConfigs.put(appType, serverConfig);
        }

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
        List<ServerConfig> serverConfigList = serverConfigList();

        assertThat(validator.isValid(serverConfigList, cvc)).isTrue();
    }



    @Test
    public void isNotValidWhenNoP2PServersAreDefined() {
        List<ServerConfig> serverConfigList =
                serverConfigList().stream().filter(s -> s.getApp() != AppType.P2P).collect(Collectors.toList());
        assertThat(validator.isValid(serverConfigList, cvc)).isFalse();
        verify(cvc).disableDefaultConstraintViolation();
        verify(cvc).buildConstraintViolationWithTemplate(eq("Only one P2P server must be configured and enabled."));
    }

    @Test
    public void isNotValidWhenTwoOrMoreP2PServersAreDefinedAndEnabled() {
        List<ServerConfig> serverConfigList = serverConfigList();
        serverConfigList.add(serverConfigs.get(AppType.P2P));
        assertThat(validator.isValid(serverConfigList, cvc)).isFalse();
        verify(cvc).disableDefaultConstraintViolation();
        verify(cvc).buildConstraintViolationWithTemplate(eq("Only one P2P server must be configured and enabled."));
    }

    @Test
    public void isNotValidWhenNoQ2TServersAreDefined() {
        List<ServerConfig> serverConfigList =
                serverConfigList().stream().filter(s -> s.getApp() != AppType.Q2T).collect(Collectors.toList());

        assertThat(validator.isValid(serverConfigList, cvc)).isFalse();
        verify(cvc).disableDefaultConstraintViolation();
        verify(cvc).buildConstraintViolationWithTemplate(eq("At least one Q2T server must be configured and enabled."));
    }

    private List<ServerConfig> serverConfigList() {
        return new ArrayList<>(serverConfigs.values());
    }
}
