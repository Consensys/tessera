package com.quorum.tessera.partyinfo.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.quorum.tessera.partyinfo.node.NodeInfo;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class PartyInfoTest {

  @Test
  public void createSimple() {
    String url = "someurl";
    Set<Recipient> recipients = Set.of(mock(Recipient.class));
    Set<Party> parties = Set.of(mock(Party.class));

    PartyInfo partyInfo = new PartyInfo(url, recipients, parties);

    assertThat(partyInfo.getParties()).isEqualTo(parties);
    assertThat(partyInfo.getRecipients()).isEqualTo(recipients);
    assertThat(partyInfo.getUrl()).isSameAs(url);
  }

  @Test
  public void fromNodeInfo() {
    com.quorum.tessera.partyinfo.node.Recipient recipient =
        mock(com.quorum.tessera.partyinfo.node.Recipient.class);
    when(recipient.getUrl()).thenReturn("http://somedomain.com/");
    NodeInfo nodeInfo =
        NodeInfo.Builder.create().withUrl("url").withRecipients(List.of(recipient)).build();

    PartyInfo partyInfo = PartyInfo.from(nodeInfo);
    assertThat(partyInfo.getUrl()).isEqualTo("url");
  }
}
