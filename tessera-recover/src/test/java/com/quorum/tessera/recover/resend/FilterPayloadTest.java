package com.quorum.tessera.recover.resend;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class FilterPayloadTest {

    private FilterPayload filterPayload;

    private Enclave enclave;

    @Before
    public void onSetUp() {
        enclave = mock(Enclave.class);
        filterPayload = new FilterPayload(enclave);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(enclave);
    }

    @Test
    public void executeSenderIsReciever() {
        BatchWorkflowContext context = new BatchWorkflowContext();
        PublicKey publicKey = mock(PublicKey.class);

        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(encodedPayload.getSenderKey()).thenReturn(publicKey);
        when(encodedPayload.getRecipientKeys()).thenReturn(List.of(publicKey));

        context.setEncodedPayload(encodedPayload);
        context.setRecipientKey(publicKey);

        when(enclave.getPublicKeys()).thenReturn(Set.of(publicKey));

        boolean result = filterPayload.filter(context);

        assertThat(result).isTrue();

        verify(enclave).getPublicKeys();

    }

    @Test
    public void executeRequestedNodeIsReciever() {

        BatchWorkflowContext context = new BatchWorkflowContext();

        PublicKey publicKey = mock(PublicKey.class);
        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(encodedPayload.getSenderKey()).thenReturn(publicKey);
        when(encodedPayload.getRecipientKeys()).thenReturn(List.of(publicKey));
        context.setEncodedPayload(encodedPayload);
        context.setRecipientKey(publicKey);

        when(enclave.getPublicKeys()).thenReturn(Set.of(mock(PublicKey.class)));

        boolean result = filterPayload.filter(context);

        assertThat(result).isTrue();

        verify(enclave).getPublicKeys();

    }
}
