package com.quorum.tessera.recovery.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.enclave.EncodedPayload;
import org.junit.Before;
import org.junit.Test;

public class DecodePayloadHandlerTest {

  private DecodePayloadHandler handler;

  @Before
  public void onSetUp() {
    handler = new DecodePayloadHandler();
  }

  @Test
  public void handle() {

    final BatchWorkflowContext context = mock(BatchWorkflowContext.class);
    final EncryptedTransaction encryptedTransaction = mock(EncryptedTransaction.class);

    when(context.getEncryptedTransaction()).thenReturn(encryptedTransaction);

    final EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(encryptedTransaction.getPayload()).thenReturn(encodedPayload);

    boolean result = handler.execute(context);

    assertThat(result).isTrue();

    verify(context).getEncryptedTransaction();
    verify(context).setEncodedPayload(encodedPayload);
    verify(encryptedTransaction).getPayload();
    verifyNoMoreInteractions(context, encryptedTransaction, encodedPayload);
  }
}
