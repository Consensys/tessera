package com.quorum.tessera.recovery.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.encryption.PublicKey;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SenderIsNotRecipientTest {

  private SenderIsNotRecipient senderIsNotRecipient;

  private Enclave enclave;

  @Before
  public void onSetUp() {
    enclave = mock(Enclave.class);
    senderIsNotRecipient = new SenderIsNotRecipient(enclave);
  }

  @After
  public void onTearDown() {
    verifyNoMoreInteractions(enclave);
  }

  @Test
  public void isSendingToSelf() {
    BatchWorkflowContext batchWorkflowContext = new BatchWorkflowContext();
    PublicKey publicKey = mock(PublicKey.class);
    batchWorkflowContext.setRecipientKey(publicKey);

    when(enclave.getPublicKeys()).thenReturn(Set.of(publicKey));

    boolean outcome = senderIsNotRecipient.filter(batchWorkflowContext);

    assertThat(outcome).isFalse();

    verify(enclave).getPublicKeys();
  }

  @Test
  public void isNotSendingToSelf() {
    BatchWorkflowContext batchWorkflowContext = new BatchWorkflowContext();
    PublicKey publicKey = mock(PublicKey.class);
    batchWorkflowContext.setRecipientKey(publicKey);

    when(enclave.getPublicKeys()).thenReturn(Set.of(mock(PublicKey.class)));

    boolean outcome = senderIsNotRecipient.filter(batchWorkflowContext);

    assertThat(outcome).isTrue();

    verify(enclave).getPublicKeys();
  }
}
