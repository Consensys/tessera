package com.quorum.tessera.config.constraints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.*;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServerConfigValidatorTest {

  private ServerConfig serverConfig;

  private ConstraintValidatorContext cvc;

  private ServerConfigValidator validator;

  @Before
  public void onSetUp() {
    cvc = mock(ConstraintValidatorContext.class);
    serverConfig = new ServerConfig();
    serverConfig.setApp(AppType.P2P);
    serverConfig.setServerAddress("localhost:123");
    serverConfig.setCommunicationType(CommunicationType.REST);
    serverConfig.setSslConfig(null);
    serverConfig.setInfluxConfig(null);
    serverConfig.setBindingAddress("http://localhost:1111");

    validator = new ServerConfigValidator();

    when(cvc.buildConstraintViolationWithTemplate(anyString()))
        .thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder.class));
  }

  @After
  public void onTearDown() {
    verifyNoMoreInteractions(cvc);
  }

  @Test
  public void isValidWhenServerConfigIsNull() {
    assertThat(validator.isValid(null, cvc)).isTrue();
  }

  @Test
  public void isValidWhenValidDataIsSupplied() {
    assertThat(serverConfig.getApp()).isSameAs(AppType.P2P);
    assertThat(serverConfig.getServerUri()).isNotNull();
    assertThat(serverConfig.getCommunicationType()).isSameAs(CommunicationType.REST);
    assertThat(serverConfig.getSslConfig()).isNull();
    assertThat(serverConfig.getInfluxConfig()).isNull();
    assertThat(serverConfig.getBindingAddress()).isEqualTo("http://localhost:1111");
    assertThat(validator.isValid(serverConfig, cvc)).isTrue();
  }

  @Test
  public void allowCorsOnlyInThirdPartyServer() {

    serverConfig.setApp(AppType.P2P);

    CrossDomainConfig cors = new CrossDomainConfig();
    cors.setAllowCredentials(true);

    serverConfig.setCrossDomainConfig(cors);

    assertThat(validator.isValid(serverConfig, cvc)).isFalse();
    verify(cvc).disableDefaultConstraintViolation();
    verify(cvc).buildConstraintViolationWithTemplate(anyString());
  }
}
