package com.quorum.tessera.enclave;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.ClientMode;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class PayloadDigestTest {

  private ClientMode clientMode;

  private Class<? extends PayloadDigest> digestType;

  public PayloadDigestTest(Map.Entry<ClientMode, Class<? extends PayloadDigest>> pair) {
    this.digestType = pair.getValue();
    this.clientMode = pair.getKey();
  }

  @Test
  public void create() {

    ServiceLoader<PayloadDigest> serviceLoader = mock(ServiceLoader.class);

    Stream<ServiceLoader.Provider<PayloadDigest>> providerStream =
        Stream.of(DefaultPayloadDigest.class, SHA512256PayloadDigest.class)
            .map(
                type ->
                    new ServiceLoader.Provider<PayloadDigest>() {
                      @Override
                      public Class<? extends PayloadDigest> type() {
                        return type;
                      }

                      @Override
                      public PayloadDigest get() {
                        return mock(type);
                      }
                    });

    when(serviceLoader.stream()).thenReturn(providerStream);

    Config config = mock(Config.class);
    when(config.getClientMode()).thenReturn(clientMode);

    ConfigFactory configFactory = mock(ConfigFactory.class);
    when(configFactory.getConfig()).thenReturn(config);

    PayloadDigest result;
    try (var serviceLoaderMockedStatic = mockStatic(ServiceLoader.class);
        var configFactoryMockedStatic = mockStatic(ConfigFactory.class)) {
      serviceLoaderMockedStatic
          .when(() -> ServiceLoader.load(PayloadDigest.class))
          .thenReturn(serviceLoader);
      configFactoryMockedStatic.when(ConfigFactory::create).thenReturn(configFactory);

      result = PayloadDigest.create();

      serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(PayloadDigest.class));
      serviceLoaderMockedStatic.verifyNoMoreInteractions();

      configFactoryMockedStatic.verify(ConfigFactory::create);
      configFactoryMockedStatic.verifyNoMoreInteractions();
    }

    assertThat(result).isExactlyInstanceOf(digestType).isNotNull();
  }

  @Parameterized.Parameters(name = "{0}")
  public static List<Map.Entry<ClientMode, Class<? extends PayloadDigest>>> params() {
    return List.of(
        Map.entry(ClientMode.ORION, SHA512256PayloadDigest.class),
        Map.entry(ClientMode.TESSERA, DefaultPayloadDigest.class));
  }
}
