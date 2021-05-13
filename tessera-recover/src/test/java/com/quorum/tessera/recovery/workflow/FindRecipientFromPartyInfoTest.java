package com.quorum.tessera.recovery.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.encryption.KeyNotFoundException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Recipient;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FindRecipientFromPartyInfoTest {

  private FindRecipientFromPartyInfo findRecipientFromPartyInfo;

  private Discovery discovery;

  @Before
  public void onSetUp() {
    discovery = mock(Discovery.class);
    findRecipientFromPartyInfo = new FindRecipientFromPartyInfo(discovery);
  }

  @After
  public void onTearDown() {
    verifyNoMoreInteractions(discovery);
  }

  @Test
  public void executeKeyFound() {

    BatchWorkflowContext batchWorkflowContext = new BatchWorkflowContext();
    PublicKey publicKey = mock(PublicKey.class);
    batchWorkflowContext.setRecipientKey(publicKey);

    NodeInfo nodeInfo = mock(NodeInfo.class);
    Recipient recipient = mock(Recipient.class);
    when(recipient.getKey()).thenReturn(publicKey);

    when(nodeInfo.getRecipients()).thenReturn(Set.of(recipient));

    when(discovery.getCurrent()).thenReturn(nodeInfo);

    boolean result = findRecipientFromPartyInfo.execute(batchWorkflowContext);
    assertThat(result).isTrue();

    assertThat(batchWorkflowContext.getRecipient()).isSameAs(recipient);

    verify(discovery).getCurrent();
  }

  @Test
  public void executeKeyNotFound() {

    BatchWorkflowContext batchWorkflowContext = new BatchWorkflowContext();
    PublicKey publicKey = mock(PublicKey.class);
    batchWorkflowContext.setRecipientKey(publicKey);

    NodeInfo nodeInfo = mock(NodeInfo.class);
    Recipient recipient = mock(Recipient.class);
    when(recipient.getKey()).thenReturn(mock(PublicKey.class));

    when(nodeInfo.getRecipients()).thenReturn(Set.of(recipient));

    when(discovery.getCurrent()).thenReturn(nodeInfo);

    try {
      findRecipientFromPartyInfo.execute(batchWorkflowContext);
      failBecauseExceptionWasNotThrown(KeyNotFoundException.class);
    } catch (KeyNotFoundException ex) {
      verify(discovery).getCurrent();
      assertThat(batchWorkflowContext.getRecipient()).isNull();
      assertThat(batchWorkflowContext.getRecipientKey()).isSameAs(publicKey);
    }
  }
}
