package com.quorum.tessera.recover.resend;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.ResendBatchPublisher;
import com.quorum.tessera.partyinfo.model.Recipient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class EncodedPayloadPublisherTest {

    private EncodedPayloadPublisher encodedPayloadPublisher;

    private Enclave enclave;

    private ResendBatchPublisher resendBatchPublisher;

    private final int batchSize = 9;

    @Before
    public void onSetUp() {

        enclave = mock(Enclave.class);
        resendBatchPublisher = mock(ResendBatchPublisher.class);

        encodedPayloadPublisher = new EncodedPayloadPublisher(enclave,resendBatchPublisher);

    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(enclave,resendBatchPublisher);
    }

    @Test
    public void executeDontSendToSelf() {
        BatchWorkflowContext encryptedTransactionEvent = new BatchWorkflowContext();
        encryptedTransactionEvent.setBatchSize(batchSize);

        PublicKey recipientKey = mock(PublicKey.class);
        encryptedTransactionEvent.setRecipientKey(recipientKey);

        when(enclave.getPublicKeys()).thenReturn(Set.of(recipientKey));

        encodedPayloadPublisher.execute(encryptedTransactionEvent);

        verify(enclave).getPublicKeys();

    }

    @Test
    public void executeSingleBatch() {

        BatchWorkflowContext encryptedTransactionEvent = new BatchWorkflowContext();
        encryptedTransactionEvent.setBatchSize(batchSize);

        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        encryptedTransactionEvent.setEncodedPayload(encodedPayload);
        PublicKey recipientKey = mock(PublicKey.class);
        Recipient recipient = mock(Recipient.class);
        when(recipient.getUrl()).thenReturn("http://junit.com");

        encryptedTransactionEvent.setRecipientKey(recipientKey);
        encryptedTransactionEvent.setRecipient(recipient);

        when(enclave.getPublicKeys()).thenReturn(Collections.EMPTY_SET);


        boolean result = IntStream.range(0,batchSize).allMatch(i -> encodedPayloadPublisher.execute(encryptedTransactionEvent));

        assertThat(result).isTrue();

        assertThat(encodedPayloadPublisher.getPublishedCount()).isEqualTo(9);

        List<EncodedPayload> sent = new ArrayList<>(batchSize);
        Collections.fill(sent,encodedPayload);

        verify(enclave,times(batchSize)).getPublicKeys();
        verify(resendBatchPublisher).publishBatch(sent,"http://junit.com");

    }

    @Test
    public void executePartialBatch() {

        BatchWorkflowContext encryptedTransactionEvent = new BatchWorkflowContext();
        encryptedTransactionEvent.setBatchSize(batchSize);
        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        encryptedTransactionEvent.setEncodedPayload(encodedPayload);
        PublicKey recipientKey = mock(PublicKey.class);
        Recipient recipient = mock(Recipient.class);
        when(recipient.getUrl()).thenReturn("http://junit.com");

        encryptedTransactionEvent.setRecipientKey(recipientKey);
        encryptedTransactionEvent.setRecipient(recipient);

        when(enclave.getPublicKeys()).thenReturn(Collections.EMPTY_SET);


        boolean result = IntStream.range(0,batchSize)
            .allMatch(i -> encodedPayloadPublisher.execute(encryptedTransactionEvent));

        assertThat(result).isTrue();

        assertThat(encodedPayloadPublisher.getPublishedCount()).isEqualTo(9);

        List<EncodedPayload> sent = new ArrayList<>(batchSize);
        Collections.fill(sent,encodedPayload);

        verify(enclave,times(batchSize)).getPublicKeys();
        verify(resendBatchPublisher).publishBatch(sent,"http://junit.com");

    }
}
