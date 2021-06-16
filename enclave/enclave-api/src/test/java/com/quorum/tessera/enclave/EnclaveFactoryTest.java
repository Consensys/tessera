package com.quorum.tessera.enclave;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.*;
import java.util.ArrayList;
import java.util.stream.Stream;
import org.junit.Test;

public class EnclaveFactoryTest {

  @Test
  public void createRemote() {

    final Config config = new Config();
    config.setEncryptor(
        new EncryptorConfig() {
          {
            setType(EncryptorType.NACL);
          }
        });

    ServerConfig serverConfig = new ServerConfig();
    serverConfig.setApp(AppType.ENCLAVE);
    serverConfig.setServerAddress("http://bogus:9898");

    config.setServerConfigs(singletonList(serverConfig));

    try (var staticEnclaveClientFactory = mockStatic(EnclaveClientFactory.class);
        var enclaveClientMockedStatic = mockStatic(EnclaveClient.class)) {

      EnclaveClientFactory enclaveClientFactory = mock(EnclaveClientFactory.class);

      staticEnclaveClientFactory
          .when(EnclaveClientFactory::create)
          .thenReturn(enclaveClientFactory);

      EnclaveClient enclaveClient = mock(EnclaveClient.class);
      enclaveClientMockedStatic.when(EnclaveClient::create).thenReturn(enclaveClient);

      EnclaveFactoryImpl enclaveFactory = new EnclaveFactoryImpl(config);
      Enclave result = enclaveFactory.createEnclave();

      assertThat(result).isSameAs(enclaveClient);
    }
  }

  @Test
  public void dontCreateRemoteWhenNoEnclaveServer() {

    Stream.of(AppType.values())
        .filter(t -> t != AppType.ENCLAVE)
        .forEach(
            t -> {
              final Config config = new Config();
              config.setEncryptor(
                  new EncryptorConfig() {
                    {
                      setType(EncryptorType.NACL);
                    }
                  });

              ServerConfig serverConfig = new ServerConfig();
              serverConfig.setApp(t);
              serverConfig.setCommunicationType(CommunicationType.REST);
              serverConfig.setServerAddress("http://bogus:9898");

              config.setServerConfigs(singletonList(serverConfig));

              KeyConfiguration keyConfiguration = new KeyConfiguration();

              KeyData keyData = new KeyData();
              keyData.setPrivateKey("yAWAJjwPqUtNVlqGjSrBmr1/iIkghuOh1803Yzx9jLM=");
              keyData.setPublicKey("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");

              keyConfiguration.setKeyData(singletonList(keyData));
              config.setKeys(keyConfiguration);

              config.setAlwaysSendTo(new ArrayList<>());

              EnclaveFactoryImpl enclaveFactory = new EnclaveFactoryImpl(config);

              Enclave result = enclaveFactory.createEnclave();

              assertThat(result).isInstanceOf(EnclaveImpl.class);
            });
  }

  @Test
  public void createLocal() {

    Config config = new Config();
    config.setEncryptor(
        new EncryptorConfig() {
          {
            setType(EncryptorType.NACL);
          }
        });

    KeyConfiguration keyConfiguration = new KeyConfiguration();

    KeyData keyData = new KeyData();
    keyData.setPublicKey("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
    keyData.setPrivateKey("yAWAJjwPqUtNVlqGjSrBmr1/iIkghuOh1803Yzx9jLM=");

    keyConfiguration.setKeyData(singletonList(keyData));
    config.setKeys(keyConfiguration);

    config.setAlwaysSendTo(new ArrayList<>());

    EnclaveFactoryImpl enclaveFactory = new EnclaveFactoryImpl(config);

    Enclave result = enclaveFactory.createEnclave();

    assertThat(result).isInstanceOf(EnclaveImpl.class);
  }

  @Test
  public void createLocalExplicitly() {

    Config config = new Config();
    config.setEncryptor(
        new EncryptorConfig() {
          {
            setType(EncryptorType.NACL);
          }
        });

    KeyConfiguration keyConfiguration = new KeyConfiguration();

    KeyData keyData = new KeyData();
    keyData.setPrivateKey("yAWAJjwPqUtNVlqGjSrBmr1/iIkghuOh1803Yzx9jLM=");
    keyData.setPublicKey("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
    keyConfiguration.setKeyData(singletonList(keyData));
    config.setKeys(keyConfiguration);

    config.setAlwaysSendTo(new ArrayList<>());
    EnclaveFactoryImpl enclaveFactory = new EnclaveFactoryImpl(config);
    Enclave result = enclaveFactory.createLocal();

    assertThat(result).isInstanceOf(EnclaveImpl.class);
  }

  @Test
  public void handleException() {
    Config config = mock(Config.class);
    EncryptorConfig encryptorConfig = mock(EncryptorConfig.class);
    when(encryptorConfig.getType()).thenThrow(new RuntimeException("OUCH"));
    when(config.getEncryptor()).thenReturn(encryptorConfig);
    EnclaveFactoryImpl enclaveFactory = new EnclaveFactoryImpl(config);

    try {
      enclaveFactory.createEnclave();
      failBecauseExceptionWasNotThrown(RuntimeException.class);
    } catch (RuntimeException ex) {
      assertThat(ex).hasMessage("OUCH");
    }
  }
}
