package com.quorum.tessera.recovery.workflow.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Recipient;
import com.quorum.tessera.recovery.resend.ResendBatchPublisher;
import com.quorum.tessera.recovery.workflow.BatchWorkflow;
import com.quorum.tessera.recovery.workflow.BatchWorkflowContext;
import com.quorum.tessera.recovery.workflow.BatchWorkflowFactory;
import com.quorum.tessera.service.Service;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.Test;

public class BatchWorkflowFactoryImplTest {

  private Enclave enclave = mock(Enclave.class);
  private PayloadEncoder payloadEncoder = mock(PayloadEncoder.class);
  private Discovery discovery = mock(Discovery.class);
  private ResendBatchPublisher resendBatchPublisher = mock(ResendBatchPublisher.class);

  @After
  public void onTearDown() {
    verifyNoMoreInteractions(enclave, payloadEncoder, discovery, resendBatchPublisher);
  }

  @Test
  public void loadMockBatchWorkflowFactory() {

    BatchWorkflowFactory batchWorkflowFactory =
        new BatchWorkflowFactoryImpl(enclave, payloadEncoder, discovery, resendBatchPublisher);

    assertThat(batchWorkflowFactory).isExactlyInstanceOf(BatchWorkflowFactoryImpl.class);
  }

  @Test
  public void createBatchWorkflowFactoryImplAndExecuteWorkflow() {

    BatchWorkflowFactoryImpl batchWorkflowFactory =
        new BatchWorkflowFactoryImpl(enclave, payloadEncoder, discovery, resendBatchPublisher);

    BatchWorkflow batchWorkflow = batchWorkflowFactory.create(1L);

    assertThat(batchWorkflow).isNotNull();

    BatchWorkflowContext batchWorkflowContext = new BatchWorkflowContext();
    PublicKey recipientKey = mock(PublicKey.class);
    batchWorkflowContext.setRecipientKey(recipientKey);
    PublicKey ownedKey = mock(PublicKey.class);

    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(encodedPayload.getSenderKey()).thenReturn(ownedKey);
    when(encodedPayload.getRecipientKeys()).thenReturn(List.of(recipientKey));

    EncryptedTransaction encryptedTransaction = mock(EncryptedTransaction.class);
    when(encryptedTransaction.getPayload()).thenReturn(encodedPayload);

    batchWorkflowContext.setEncryptedTransaction(encryptedTransaction);
    batchWorkflowContext.setEncodedPayload(encodedPayload);
    batchWorkflowContext.setBatchSize(100);

    when(payloadEncoder.forRecipient(any(), any())).thenReturn(encodedPayload);
    when(enclave.status()).thenReturn(Service.Status.STARTED);
    when(enclave.getPublicKeys()).thenReturn(Set.of(ownedKey));

    NodeInfo nodeInfo = mock(NodeInfo.class);
    when(nodeInfo.getRecipients()).thenReturn(Set.of(Recipient.of(recipientKey, "url")));

    when(discovery.getCurrent()).thenReturn(nodeInfo);

    assertThat(batchWorkflow.execute(batchWorkflowContext)).isTrue();
    assertThat(batchWorkflow.getPublishedMessageCount()).isOne();

    verify(enclave).status();
    verify(enclave, times(2)).getPublicKeys();
    verify(payloadEncoder).forRecipient(any(), any());
    verify(discovery).getCurrent();

    verify(resendBatchPublisher).publishBatch(any(), any());
  }

  @Test
  public void workflowExecutedReturnFalse() {

    BatchWorkflowFactoryImpl batchWorkflowFactory =
        new BatchWorkflowFactoryImpl(enclave, payloadEncoder, discovery, resendBatchPublisher);

    BatchWorkflow batchWorkflow = batchWorkflowFactory.create(999L);

    assertThat(batchWorkflow).isNotNull();

    BatchWorkflowContext batchWorkflowContext = new BatchWorkflowContext();
    PublicKey publicKey = mock(PublicKey.class);
    batchWorkflowContext.setRecipientKey(publicKey);

    EncryptedTransaction encryptedTransaction = mock(EncryptedTransaction.class);

    batchWorkflowContext.setEncryptedTransaction(encryptedTransaction);
    batchWorkflowContext.setEncodedPayload(mock(EncodedPayload.class));

    when(enclave.status()).thenReturn(Service.Status.STARTED);

    assertThat(batchWorkflow.execute(batchWorkflowContext)).isFalse();
    assertThat(batchWorkflow.getPublishedMessageCount()).isZero();

    verify(enclave).status();
  }
}
