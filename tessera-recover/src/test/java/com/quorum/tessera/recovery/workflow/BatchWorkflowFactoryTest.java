package com.quorum.tessera.recovery.workflow;

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
import com.quorum.tessera.service.Service;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

public class BatchWorkflowFactoryTest {

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

  // FIXME:
  @Ignore
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

    EncryptedTransaction encryptedTransaction = mock(EncryptedTransaction.class);
    byte[] payloadData = "PAYLOAD".getBytes();
    when(encryptedTransaction.getEncodedPayload()).thenReturn(payloadData);

    batchWorkflowContext.setEncryptedTransaction(encryptedTransaction);

    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(encodedPayload.getSenderKey()).thenReturn(ownedKey);
    when(encodedPayload.getRecipientKeys()).thenReturn(List.of(recipientKey));

    when(payloadEncoder.decode(payloadData)).thenReturn(encodedPayload);
    when(payloadEncoder.forRecipient(any(), any())).thenReturn(encodedPayload);
    when(enclave.status()).thenReturn(Service.Status.STARTED);
    when(enclave.getPublicKeys()).thenReturn(Set.of(ownedKey));

    NodeInfo nodeInfo = mock(NodeInfo.class);
    when(nodeInfo.getRecipients()).thenReturn(Set.of(Recipient.of(recipientKey, "url")));

    when(discovery.getCurrent()).thenReturn(nodeInfo);

    assertThat(batchWorkflow.execute(batchWorkflowContext)).isTrue();
    assertThat(batchWorkflow.getPublishedMessageCount()).isOne();

    verify(payloadEncoder).decode(payloadData);
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

  @Test
  public void create() {
    BatchWorkflowFactory expected = mock(BatchWorkflowFactory.class);
    BatchWorkflowFactory result;
    try (var staticServiceLoader = mockStatic(ServiceLoader.class)) {
      ServiceLoader<BatchWorkflowFactory> serviceLoader = mock(ServiceLoader.class);
      when(serviceLoader.findFirst()).thenReturn(Optional.of(expected));
      staticServiceLoader
          .when(() -> ServiceLoader.load(BatchWorkflowFactory.class))
          .thenReturn(serviceLoader);

      result = BatchWorkflowFactory.create();

      staticServiceLoader.verify(() -> ServiceLoader.load(BatchWorkflowFactory.class));
      staticServiceLoader.verifyNoMoreInteractions();

      verify(serviceLoader).findFirst();
      verifyNoMoreInteractions(serviceLoader);
    }

    assertThat(result).isSameAs(expected);
  }
}
