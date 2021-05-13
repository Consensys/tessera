package com.quorum.tessera.encryption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Stream;
import org.junit.Test;

public class EncryptorFactoryTest {

  @Test
  public void newFactoryAndCreate() {
    EncryptorFactory expected = mock(EncryptorFactory.class);
    when(expected.getType()).thenReturn("MOCK");

    Encryptor encryptor = mock(Encryptor.class);
    when(expected.create()).thenReturn(encryptor);

    ServiceLoader<EncryptorFactory> serviceLoader = mock(ServiceLoader.class);
    ServiceLoader.Provider<EncryptorFactory> provider = mock(ServiceLoader.Provider.class);
    when(provider.get()).thenReturn(expected);
    when(serviceLoader.stream()).thenReturn(Stream.of(provider));

    EncryptorFactory encryptorFactory;
    Encryptor result;
    try (var mockedStaticServiceLoader = mockStatic(ServiceLoader.class)) {

      mockedStaticServiceLoader
          .when(() -> ServiceLoader.load(EncryptorFactory.class))
          .thenReturn(serviceLoader);

      encryptorFactory = EncryptorFactory.newFactory("MOCK");

      result = encryptorFactory.create();

      verify(serviceLoader).stream();
      verifyNoMoreInteractions(serviceLoader);

      mockedStaticServiceLoader.verify(() -> ServiceLoader.load(EncryptorFactory.class));
      mockedStaticServiceLoader.verifyNoMoreInteractions();

      verify(encryptorFactory).create();
      verify(encryptorFactory).getType();
      verifyNoMoreInteractions(encryptorFactory);
    }

    assertThat(encryptorFactory).isNotNull();
    assertThat(encryptorFactory).isSameAs(expected);
    assertThat(result).isSameAs(encryptor);
  }

  @Test
  public void exceptionIfServiceNotFound() {
    Throwable ex = catchThrowable(() -> EncryptorFactory.newFactory("NOTAVAILABLE"));

    assertThat(ex).isExactlyInstanceOf(EncryptorFactoryNotFoundException.class);
    assertThat(ex).hasMessageContaining("NOTAVAILABLE");
  }

  @Test
  public void create() {

    Encryptor encryptor = mock(Encryptor.class);
    EncryptorFactory encryptorFactory =
        new EncryptorFactory() {
          @Override
          public Encryptor create(Map<String, String> properties) {
            return encryptor;
          }

          @Override
          public String getType() {
            return null;
          }
        };

    assertThat(encryptorFactory.create()).isSameAs(encryptor);
  }
}
