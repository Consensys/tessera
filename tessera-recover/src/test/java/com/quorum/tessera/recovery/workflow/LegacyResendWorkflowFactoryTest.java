package com.quorum.tessera.recovery.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Recipient;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LegacyResendWorkflowFactoryTest {

  private Enclave enclave;

  private Discovery discovery;

  private PayloadEncoder payloadEncoder;

  private PayloadPublisher payloadPublisher;

  private LegacyWorkflowFactory wfFactory;

  @Before
  public void init() {
    this.enclave = mock(Enclave.class);
    this.discovery = mock(Discovery.class);
    this.payloadEncoder = mock(PayloadEncoder.class);
    this.payloadPublisher = mock(PayloadPublisher.class);

    this.wfFactory = new LegacyWorkflowFactory(enclave, discovery, payloadPublisher);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(enclave, discovery, payloadEncoder, payloadPublisher);
  }

  @Test
  public void staticGetPublishedMessageCountIs0() {
    assertThat(wfFactory.create().getPublishedMessageCount()).isEqualTo(0);
  }

  @Test
  public void successForAllStagesReturnsTrue() {

    final PublicKey targetResendKey = PublicKey.from("target".getBytes());
    final PublicKey localRecipient = PublicKey.from("local-recipient".getBytes());
    final EncodedPayload payload =
        EncodedPayload.Builder.create()
            .withSenderKey(targetResendKey)
            .withRecipientBox("testbox".getBytes())
            .build();

    final NodeInfo nodeInfo = mock(NodeInfo.class);
    when(nodeInfo.getRecipients()).thenReturn(Set.of(Recipient.of(targetResendKey, "url")));
    when(discovery.getCurrent()).thenReturn(nodeInfo);

    final EncryptedTransaction encryptedTx = mock(EncryptedTransaction.class);

    when(enclave.getPublicKeys()).thenReturn(Set.of(localRecipient));
    when(enclave.unencryptTransaction(any(EncodedPayload.class), eq(localRecipient)))
        .thenReturn(new byte[0]);

    final BatchWorkflow batchWorkflow = wfFactory.create();
    final BatchWorkflowContext context = new BatchWorkflowContext();
    context.setEncryptedTransaction(encryptedTx);
    context.setEncodedPayload(payload);
    context.setRecipientKey(targetResendKey);
    context.setBatchSize(1);
    final boolean success = batchWorkflow.execute(context);

    assertThat(success).isTrue();

    verify(discovery).getCurrent();
    verify(payloadPublisher).publishPayload(any(EncodedPayload.class), eq(targetResendKey));
    verify(enclave).status();
    verify(enclave, times(2)).getPublicKeys();
    verify(enclave).unencryptTransaction(any(EncodedPayload.class), eq(localRecipient));
  }

  @Test
  public void failureAtStepReturnsFalse() {

    final PublicKey targetResendKey = PublicKey.from("target".getBytes());
    final EncodedPayload testPayload = mock(EncodedPayload.class);
    when(testPayload.getPrivacyMode()).thenReturn(PrivacyMode.PARTY_PROTECTION); // causes a failure
    when(testPayload.getSenderKey()).thenReturn(targetResendKey);

    final EncryptedTransaction encryptedTx = new EncryptedTransaction();

    final BatchWorkflow batchWorkflow = wfFactory.create();
    final BatchWorkflowContext context = new BatchWorkflowContext();
    context.setEncryptedTransaction(encryptedTx);
    context.setRecipientKey(targetResendKey);
    context.setBatchSize(1);
    final boolean success = batchWorkflow.execute(context);

    assertThat(success).isFalse();
    verify(enclave).status();
  }
}
