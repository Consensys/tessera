package com.quorum.tessera.recover.resend;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.ResendBatchPublisher;
import com.quorum.tessera.service.Service;
import org.junit.After;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class BatchWorkflowFactoryTest {

    private Enclave enclave = mock(Enclave.class);
    private PayloadEncoder payloadEncoder = mock(PayloadEncoder.class);
    private PartyInfoService partyInfoService = mock(PartyInfoService.class);
    private ResendBatchPublisher resendBatchPublisher = mock(ResendBatchPublisher.class);

    @After
    public void onTearDown() {
        MockBatchWorkflowFactory.reset();
        verifyNoMoreInteractions(enclave,payloadEncoder,partyInfoService,resendBatchPublisher);
    }

    @Test
    public void loadMockBatchWorkflowFactory() {

        BatchWorkflowFactory batchWorkflowFactory = BatchWorkflowFactory.newFactory(enclave, payloadEncoder, partyInfoService, resendBatchPublisher);

        assertThat(batchWorkflowFactory).isExactlyInstanceOf(MockBatchWorkflowFactory.class);
    }

    @Test
    public void createBatchWorkflowFactoryImplAndExecuteWorkflow() {

        BatchWorkflowFactoryImpl batchWorkflowFactory = new BatchWorkflowFactoryImpl();
        batchWorkflowFactory.setResendBatchPublisher(resendBatchPublisher);
        batchWorkflowFactory.setPayloadEncoder(payloadEncoder);
        batchWorkflowFactory.setEnclave(enclave);
        batchWorkflowFactory.setPartyInfoService(partyInfoService);

        BatchWorkflow batchWorkflow = batchWorkflowFactory.create();

        assertThat(batchWorkflow).isNotNull();

        BatchWorkflowContext batchWorkflowContext = new BatchWorkflowContext();
        PublicKey publicKey = mock(PublicKey.class);
        batchWorkflowContext.setRecipientKey(publicKey);

        EncryptedTransaction encryptedTransaction = mock(EncryptedTransaction.class);
        byte[] payloadData = "PAYLOAD".getBytes();
        when(encryptedTransaction.getEncodedPayload()).thenReturn(payloadData);

        batchWorkflowContext.setEncryptedTransaction(encryptedTransaction);

        when(payloadEncoder.decode(payloadData)).thenReturn(mock(EncodedPayload.class));
        when(enclave.status()).thenReturn(Service.Status.STARTED);


        assertThat(batchWorkflow.execute(batchWorkflowContext)).isFalse();
        assertThat(batchWorkflow.getPublishedMessageCount()).isZero();

        verify(payloadEncoder).decode(payloadData);
        verify(enclave).status();

    }

}
