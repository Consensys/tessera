package com.quorum.tessera.context.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.config.util.KeyDataUtil;
import com.quorum.tessera.context.KeyVaultConfigValidations;
import com.quorum.tessera.context.RestClientFactory;
import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.enclave.Enclave;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.client.Client;
import java.net.URI;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class RuntimeContextProviderTest {

  private ClientMode clientMode;

  public RuntimeContextProviderTest(ClientMode clientMode) {
    this.clientMode = clientMode;
  }

  @Before
  @After
  public void clearHolder() {
    RuntimeContextHolder.INSTANCE.setContext(null);
    assertThat(RuntimeContextHolder.INSTANCE.getContext()).isNotPresent();
  }

  @Test
  public void provides() {

    Config confg = createMockConfig();

    try (var mockedStaticConfigFactory = mockStatic(ConfigFactory.class);
        var mockStaticRestClientFactory = mockStatic(RestClientFactory.class);
        var mockStaticKeyDataUtil = mockStatic(KeyDataUtil.class);
        var mockStaticEnclave = mockStatic(Enclave.class)) {

      Enclave enclave = mock(Enclave.class);
      mockStaticEnclave.when(Enclave::create).thenReturn(enclave);

      ConfigKeyPair configKeyPair = mock(ConfigKeyPair.class);
      when(configKeyPair.getPublicKey())
          .thenReturn(Base64.getEncoder().encodeToString("PublicKey".getBytes()));
      when(configKeyPair.getPrivateKey())
          .thenReturn(Base64.getEncoder().encodeToString("PrivateKey".getBytes()));

      mockStaticKeyDataUtil
          .when(() -> KeyDataUtil.unmarshal(any(KeyData.class), any(KeyEncryptor.class)))
          .thenReturn(configKeyPair);

      RestClientFactory restClientFactory = mock(RestClientFactory.class);
      when(restClientFactory.buildFrom(any(ServerConfig.class))).thenReturn(mock(Client.class));
      mockStaticRestClientFactory.when(RestClientFactory::create).thenReturn(restClientFactory);

      ConfigFactory configFactory = mock(ConfigFactory.class);
      when(configFactory.getConfig()).thenReturn(confg);
      mockedStaticConfigFactory.when(ConfigFactory::create).thenReturn(configFactory);

      RuntimeContext runtimeContext = RuntimeContextProvider.provider();
      assertThat(runtimeContext).isNotNull().isSameAs(RuntimeContextProvider.provider());

      mockedStaticConfigFactory.verify(ConfigFactory::create);
      mockedStaticConfigFactory.verifyNoMoreInteractions();

      mockStaticRestClientFactory.verify(RestClientFactory::create);
      mockedStaticConfigFactory.verifyNoMoreInteractions();

      mockStaticKeyDataUtil.verify(
          () -> KeyDataUtil.unmarshal(any(KeyData.class), any(KeyEncryptor.class)));
      mockStaticKeyDataUtil.verifyNoMoreInteractions();

      mockStaticEnclave.verify(Enclave::create);
      mockStaticEnclave.verifyNoMoreInteractions();

      verify(enclave).getPublicKeys();
      verifyNoMoreInteractions(enclave);
    }
  }

  @Test
  public void providesHasVaultValidationFailures() {

    Config confg = createMockConfig();
    try (var mockedStaticConfigFactory = mockStatic(ConfigFactory.class);
        var mockStaticKeyVaultConfigValidations = mockStatic(KeyVaultConfigValidations.class)) {

      KeyVaultConfigValidations keyVaultConfigValidations = mock(KeyVaultConfigValidations.class);
      ConstraintViolation constraintViolation = mock(ConstraintViolation.class);
      when(keyVaultConfigValidations.validate(any(), anyList()))
          .thenReturn(Set.of(constraintViolation));

      mockStaticKeyVaultConfigValidations
          .when(KeyVaultConfigValidations::create)
          .thenReturn(keyVaultConfigValidations);

      ConfigFactory configFactory = mock(ConfigFactory.class);
      when(configFactory.getConfig()).thenReturn(confg);
      mockedStaticConfigFactory.when(ConfigFactory::create).thenReturn(configFactory);

      Throwable ex = catchThrowable(() -> RuntimeContextProvider.provider());
      assertThat(ex).isExactlyInstanceOf(ConstraintViolationException.class);

      ConstraintViolationException constraintViolationException = (ConstraintViolationException) ex;
      assertThat(constraintViolationException.getConstraintViolations())
          .containsExactly(constraintViolation);

      mockedStaticConfigFactory.verify(ConfigFactory::create);
      mockedStaticConfigFactory.verifyNoMoreInteractions();

      mockedStaticConfigFactory.verifyNoMoreInteractions();
    }
  }

  @Test
  public void providerWithNoP2pServerConfig() {
    Config config = mock(Config.class);
    ServerConfig serverConfig = mock(ServerConfig.class);
    when(config.getServerConfigs()).thenReturn(List.of(serverConfig));
    try (var mockedStaticConfigFactory = mockStatic(ConfigFactory.class)) {
      ConfigFactory configFactory = mock(ConfigFactory.class);
      when(configFactory.getConfig()).thenReturn(config);
      mockedStaticConfigFactory.when(ConfigFactory::create).thenReturn(configFactory);

      Throwable ex = catchThrowable(() -> RuntimeContextProvider.provider());

      assertThat(ex)
          .isExactlyInstanceOf(IllegalStateException.class)
          .hasMessage("No P2P server configured");

      mockedStaticConfigFactory.verify(ConfigFactory::create);
      mockedStaticConfigFactory.verifyNoMoreInteractions();
    }
  }

  Config createMockConfig() {
    Config confg = mock(Config.class);
    EncryptorConfig encryptorConfig = mock(EncryptorConfig.class);
    when(encryptorConfig.getType()).thenReturn(EncryptorType.NACL);

    when(confg.getEncryptor()).thenReturn(encryptorConfig);

    KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    when(keyConfiguration.getKeyData()).thenReturn(List.of(mock(KeyData.class)));
    when(confg.getKeys()).thenReturn(keyConfiguration);

    ServerConfig serverConfig = mock(ServerConfig.class);
    when(serverConfig.getApp()).thenReturn(AppType.P2P);
    when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.REST);
    when(confg.getP2PServerConfig()).thenReturn(serverConfig);
    when(serverConfig.getServerUri()).thenReturn(URI.create("http://bogus"));
    when(serverConfig.getBindingUri()).thenReturn(URI.create("http://bogus"));
    when(serverConfig.getProperties()).thenReturn(Collections.emptyMap());

    when(confg.getServerConfigs()).thenReturn(List.of(serverConfig));

    FeatureToggles featureToggles = mock(FeatureToggles.class);
    when(confg.getFeatures()).thenReturn(featureToggles);
    when(featureToggles.isEnableMultiplePrivateStates()).thenReturn(false);
    when(confg.getClientMode()).thenReturn(clientMode);
    return confg;
  }

  @Test
  public void defaultConstructorForCoverage() {
    assertThat(new RuntimeContextProvider()).isNotNull();
  }

  @Parameterized.Parameters(name = "ClientMode: {0}")
  public static List<ClientMode> configs() {
    return List.of(ClientMode.values());
  }
}
