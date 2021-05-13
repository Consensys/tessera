package com.quorum.tessera.recovery.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FilterPayloadTest {

  private BatchWorkflowFilter filterPayload;

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
  public void testCurrentNodeIsSender() {
    BatchWorkflowContext context = new BatchWorkflowContext();
    PublicKey sender = mock(PublicKey.class);
    PublicKey recipient = mock(PublicKey.class);

    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(encodedPayload.getSenderKey()).thenReturn(sender);
    when(encodedPayload.getRecipientKeys()).thenReturn(List.of(sender, recipient));

    context.setEncodedPayload(encodedPayload);
    context.setRecipientKey(recipient);

    when(enclave.getPublicKeys()).thenReturn(Set.of(sender));

    boolean result = filterPayload.execute(context);

    assertThat(result).isTrue();

    verify(enclave).getPublicKeys();
  }

  @Test
  public void testRequestedNodeIsSender() {
    BatchWorkflowContext context = new BatchWorkflowContext();
    PublicKey sender = mock(PublicKey.class);
    PublicKey recipient = mock(PublicKey.class);

    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(encodedPayload.getSenderKey()).thenReturn(recipient);
    when(encodedPayload.getRecipientKeys()).thenReturn(List.of(sender, recipient));

    context.setEncodedPayload(encodedPayload);
    context.setRecipientKey(recipient);

    when(enclave.getPublicKeys()).thenReturn(Set.of(sender));

    boolean result = filterPayload.filter(context);

    assertThat(result).isTrue();

    verify(enclave).getPublicKeys();
  }

  @Test
  public void testIrrelevantPayload() {
    BatchWorkflowContext context = new BatchWorkflowContext();
    final PublicKey requester = mock(PublicKey.class);
    final PublicKey recipient = mock(PublicKey.class);
    final PublicKey thisNode = mock(PublicKey.class);

    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(encodedPayload.getSenderKey()).thenReturn(thisNode);
    when(encodedPayload.getRecipientKeys()).thenReturn(List.of(thisNode, recipient));

    context.setEncodedPayload(encodedPayload);
    context.setRecipientKey(requester);

    when(enclave.getPublicKeys()).thenReturn(Set.of(thisNode));

    boolean result = filterPayload.filter(context);

    assertThat(result).isFalse();
  }
}
