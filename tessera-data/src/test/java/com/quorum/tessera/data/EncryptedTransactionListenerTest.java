package com.quorum.tessera.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.EncodedPayloadCodec;
import com.quorum.tessera.enclave.PayloadEncoder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

public class EncryptedTransactionListenerTest {

  private final MockedStatic<PayloadEncoder> payloadEncoderFactoryFunction =
      mockStatic(PayloadEncoder.class);

  private EncryptedTransactionListener encryptedTransactionListener;

  private PayloadEncoder payloadEncoder;

  @Before
  public void beforeTest() {
    encryptedTransactionListener = new EncryptedTransactionListener();
    payloadEncoder = mock(PayloadEncoder.class);
    payloadEncoderFactoryFunction
        .when(() -> PayloadEncoder.create(any(EncodedPayloadCodec.class)))
        .thenReturn(payloadEncoder);
  }

  @After
  public void afterTest() {
    try {
      verifyNoMoreInteractions(payloadEncoder);
      payloadEncoderFactoryFunction.verifyNoMoreInteractions();
    } finally {
      payloadEncoderFactoryFunction.close();
    }
  }

  @Test
  public void onLoad() {

    byte[] payloadData = "PayloadData".getBytes();
    EncodedPayload payload = mock(EncodedPayload.class);
    when(payloadEncoder.decode(payloadData)).thenReturn(payload);

    EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
    encryptedTransaction.setEncodedPayloadCodec(EncodedPayloadCodec.CBOR);
    encryptedTransaction.setEncodedPayload(payloadData);

    encryptedTransactionListener.onLoad(encryptedTransaction);

    verify(payloadEncoder).decode(payloadData);

    payloadEncoderFactoryFunction.verify(
        () -> PayloadEncoder.create(any(EncodedPayloadCodec.class)));

    assertThat(encryptedTransaction.getPayload()).isEqualTo(payload);
  }

  @Test
  public void onLoadLegacyEncodedData() {

    byte[] payloadData = "PayloadData".getBytes();
    EncodedPayload payload = mock(EncodedPayload.class);
    when(payloadEncoder.decode(payloadData)).thenReturn(payload);

    EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
    encryptedTransaction.setEncodedPayload(payloadData);

    encryptedTransactionListener.onLoad(encryptedTransaction);

    verify(payloadEncoder).decode(payloadData);

    payloadEncoderFactoryFunction.verify(
        () -> PayloadEncoder.create(eq(EncodedPayloadCodec.LEGACY)));

    assertThat(encryptedTransaction.getPayload()).isEqualTo(payload);
    assertThat(encryptedTransaction.getEncodedPayloadCodec()).isEqualTo(EncodedPayloadCodec.LEGACY);
  }

  @Test
  public void onSave() {
    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
    encryptedTransaction.setPayload(encodedPayload);

    byte[] payloadData = "PayloadData".getBytes();
    when(payloadEncoder.encode(encodedPayload)).thenReturn(payloadData);

    encryptedTransactionListener.onSave(encryptedTransaction);

    verify(payloadEncoder).encode(encodedPayload);
    payloadEncoderFactoryFunction.verify(
        () -> PayloadEncoder.create(EncodedPayloadCodec.current()));
    assertThat(encryptedTransaction.getEncodedPayload()).isEqualTo(payloadData);
  }
}
