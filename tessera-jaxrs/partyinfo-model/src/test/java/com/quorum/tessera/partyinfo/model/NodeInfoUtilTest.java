package com.quorum.tessera.partyinfo.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.VersionInfo;
import java.util.Set;
import org.junit.Test;

public class NodeInfoUtilTest {

  @Test
  public void from() {

    PartyInfo partyInfo = mock(PartyInfo.class);
    VersionInfo versionInfo = mock(VersionInfo.class);
    Recipient recipient = Recipient.of(mock(PublicKey.class), "someurl");

    when(partyInfo.getUrl()).thenReturn("someurl");
    when(versionInfo.supportedApiVersions()).thenReturn(Set.of("v1", "v3"));
    when(partyInfo.getRecipients()).thenReturn(Set.of(recipient));

    NodeInfo nodeInfo = NodeInfoUtil.from(partyInfo, Set.of("v1", "v3"));

    assertThat(nodeInfo).isNotNull();

    assertThat(nodeInfo.getUrl()).isEqualTo("someurl");
    assertThat(nodeInfo.supportedApiVersions()).isEqualTo(Set.of("v3", "v1"));
    assertThat(nodeInfo.getRecipients()).hasSize(1);
    assertThat(nodeInfo.getRecipients().iterator().next().getUrl()).isEqualTo("someurl");
  }
}
