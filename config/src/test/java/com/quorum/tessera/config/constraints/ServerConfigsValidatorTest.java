package com.quorum.tessera.config.constraints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import jakarta.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServerConfigsValidatorTest {

  private Map<AppType, ServerConfig> serverConfigsMap = new HashMap<>();

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

      serverConfigsMap.put(appType, serverConfig);
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
  public void ignoreNullArg() {
    assertThat(validator.isValid(null, cvc)).isTrue();
  }

  @Test
  public void isValidWhenServerConfigsIsNull() {
    Config config = new Config();
    config.setServerConfigs(null);

    assertThat(validator.isValid(config, cvc)).isTrue();
  }

  @Test
  public void isValidWhenValidDataIsSupplied() {
    List<ServerConfig> serverConfigList = serverConfigList();
    Config config = new Config();
    config.setServerConfigs(serverConfigList);

    assertThat(validator.isValid(config, cvc)).isTrue();
  }

  @Test
  public void isNotValidWhenNoP2PServersAreDefined() {
    List<ServerConfig> serverConfigList =
        serverConfigList().stream()
            .filter(s -> s.getApp() != AppType.P2P)
            .collect(Collectors.toList());
    Config config = new Config();
    config.setServerConfigs(serverConfigList);

    assertThat(validator.isValid(config, cvc)).isFalse();
    verify(cvc).disableDefaultConstraintViolation();
    verify(cvc)
        .buildConstraintViolationWithTemplate(eq("Exactly one P2P server must be configured."));
  }

  @Test
  public void isNotValidWhenTwoOrMoreP2PServersAreDefinedAndEnabled() {
    List<ServerConfig> serverConfigList = serverConfigList();
    serverConfigList.add(serverConfigsMap.get(AppType.P2P));
    Config config = new Config();
    config.setServerConfigs(serverConfigList);

    assertThat(validator.isValid(config, cvc)).isFalse();
    verify(cvc).disableDefaultConstraintViolation();
    verify(cvc)
        .buildConstraintViolationWithTemplate(eq("Exactly one P2P server must be configured."));
  }

  @Test
  public void isNotValidWhenNoQ2TServersAreDefined() {
    List<ServerConfig> serverConfigList =
        serverConfigList().stream()
            .filter(s -> s.getApp() != AppType.Q2T)
            .collect(Collectors.toList());
    Config config = new Config();
    config.setServerConfigs(serverConfigList);

    assertThat(validator.isValid(config, cvc)).isFalse();
    verify(cvc).disableDefaultConstraintViolation();
    verify(cvc)
        .buildConstraintViolationWithTemplate(
            eq("At least one Q2T server must be configured or bootstrap mode enabled."));
  }

  @Test
  public void isNotValidWhenQ2TServersAreDefinedOnBootstrapNode() {
    List<ServerConfig> serverConfigList = serverConfigList();
    Config config = new Config();
    config.setBootstrapNode(true);
    config.setServerConfigs(serverConfigList);

    assertThat(validator.isValid(config, cvc)).isFalse();
    verify(cvc).disableDefaultConstraintViolation();
    verify(cvc)
        .buildConstraintViolationWithTemplate(
            eq("Q2T server cannot be specified on a bootstrap node."));
  }

  private List<ServerConfig> serverConfigList() {
    return new ArrayList<>(serverConfigsMap.values());
  }
}
