package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DecodePayloadHandlerTest {

    private DecodePayloadHandler handler;

    private PayloadEncoder payloadEncoder;

    @Before
    public void onSetUp() {
        payloadEncoder = mock(PayloadEncoder.class);
        handler = new DecodePayloadHandler(payloadEncoder);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(payloadEncoder);
    }

    @Test
    public void handle() {

        final byte[] payloadData = "SOMEDATA".getBytes();

        final BatchWorkflowContext context = mock(BatchWorkflowContext.class);
        final EncryptedTransaction encryptedTransaction = mock(EncryptedTransaction.class);

        when(context.getEncryptedTransaction()).thenReturn(encryptedTransaction);
        when(encryptedTransaction.getEncodedPayload()).thenReturn(payloadData);

        final EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(payloadEncoder.decode(payloadData)).thenReturn(encodedPayload);

        boolean result = handler.execute(context);

        assertThat(result).isTrue();

        verify(context).getEncryptedTransaction();
        verify(context).setEncodedPayload(encodedPayload);
        verify(payloadEncoder).decode(payloadData);
        verify(encryptedTransaction).getEncodedPayload();

        verifyNoMoreInteractions(context,encryptedTransaction,encodedPayload);

    }


}
