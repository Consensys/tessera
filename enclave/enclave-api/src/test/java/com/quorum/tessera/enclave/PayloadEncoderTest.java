package com.quorum.tessera.enclave;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Stream;
import org.junit.Test;
import org.mockito.Mockito;

public class PayloadEncoderTest {

  @Test
  public void create() {

    ServiceLoader<PayloadEncoder> serviceLoader = mock(ServiceLoader.class);
    PayloadEncoder payloadEncoder = mock(PayloadEncoder.class);
    when(payloadEncoder.encodedPayloadCodec()).thenReturn(EncodedPayloadCodec.UNSUPPORTED);

    ServiceLoader.Provider<PayloadEncoder> payloadEncoderProvider =
        mock(ServiceLoader.Provider.class);
    when(payloadEncoderProvider.get()).thenReturn(payloadEncoder);

    when(serviceLoader.stream()).thenReturn(Stream.of(payloadEncoderProvider));
    PayloadEncoder result;
    try (var serviceLoaderMockedStatic = Mockito.mockStatic(ServiceLoader.class)) {
      serviceLoaderMockedStatic
          .when(() -> ServiceLoader.load(PayloadEncoder.class))
          .thenReturn(serviceLoader);
      result = PayloadEncoder.create(EncodedPayloadCodec.UNSUPPORTED).get();

      serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(PayloadEncoder.class));
      serviceLoaderMockedStatic.verifyNoMoreInteractions();
    }

    assertThat(result).isNotNull().isSameAs(payloadEncoder);
    verify(payloadEncoder).encodedPayloadCodec();
    verify(payloadEncoderProvider).get();

    verifyNoMoreInteractions(payloadEncoder, payloadEncoderProvider);
  }

  @Test
  public void createWithDuplicateEncoders() {

    ServiceLoader<PayloadEncoder> serviceLoader = mock(ServiceLoader.class);
    PayloadEncoder payloadEncoder = mock(PayloadEncoder.class);
    when(payloadEncoder.encodedPayloadCodec()).thenReturn(EncodedPayloadCodec.LEGACY);

    PayloadEncoder anotherPayloadEncoder = mock(PayloadEncoder.class);
    when(anotherPayloadEncoder.encodedPayloadCodec()).thenReturn(EncodedPayloadCodec.LEGACY);

    ServiceLoader.Provider<PayloadEncoder> payloadEncoderProvider =
        mock(ServiceLoader.Provider.class);
    when(payloadEncoderProvider.get()).thenReturn(payloadEncoder);

    ServiceLoader.Provider<PayloadEncoder> anotherPayloadEncoderProvider =
        mock(ServiceLoader.Provider.class);
    when(anotherPayloadEncoderProvider.get()).thenReturn(anotherPayloadEncoder);

    when(serviceLoader.stream())
        .thenReturn(Stream.of(payloadEncoderProvider, anotherPayloadEncoderProvider));

    try (var serviceLoaderMockedStatic = Mockito.mockStatic(ServiceLoader.class)) {
      serviceLoaderMockedStatic
          .when(() -> ServiceLoader.load(PayloadEncoder.class))
          .thenReturn(serviceLoader);

      PayloadEncoder.create(EncodedPayloadCodec.LEGACY);
      failBecauseExceptionWasNotThrown(IllegalStateException.class);

    } catch (IllegalStateException illegalStateException) {
      assertThat(illegalStateException).hasMessage("Resolved multiple encoders for codec LEGACY");
    }

    verify(payloadEncoder).encodedPayloadCodec();
    verify(anotherPayloadEncoder).encodedPayloadCodec();
    verify(payloadEncoderProvider).get();
    verify(anotherPayloadEncoderProvider).get();

    verifyNoMoreInteractions(payloadEncoder, payloadEncoderProvider);
  }

  @Test
  public void createWithEncodedPayloadCodec() {

    ServiceLoader<PayloadEncoder> serviceLoader = mock(ServiceLoader.class);
    PayloadEncoder payloadEncoder = mock(PayloadEncoder.class);
    when(payloadEncoder.encodedPayloadCodec()).thenReturn(EncodedPayloadCodec.LEGACY);

    ServiceLoader.Provider<PayloadEncoder> payloadEncoderProvider =
        mock(ServiceLoader.Provider.class);
    when(payloadEncoderProvider.get()).thenReturn(payloadEncoder);

    when(serviceLoader.stream()).thenReturn(Stream.of(payloadEncoderProvider));
    PayloadEncoder result;
    try (var serviceLoaderMockedStatic = Mockito.mockStatic(ServiceLoader.class)) {
      serviceLoaderMockedStatic
          .when(() -> ServiceLoader.load(PayloadEncoder.class))
          .thenReturn(serviceLoader);
      result = PayloadEncoder.create(EncodedPayloadCodec.LEGACY).get();

      serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(PayloadEncoder.class));
      serviceLoaderMockedStatic.verifyNoMoreInteractions();
    }

    assertThat(result).isNotNull().isSameAs(payloadEncoder);
    verify(payloadEncoder).encodedPayloadCodec();
    verify(payloadEncoderProvider).get();

    verifyNoMoreInteractions(payloadEncoder, payloadEncoderProvider);
  }

  @Test
  public void createUnsupported() {
    Optional<PayloadEncoder> result = PayloadEncoder.create(EncodedPayloadCodec.UNSUPPORTED);
    assertThat(result).isEmpty();
  }
}
