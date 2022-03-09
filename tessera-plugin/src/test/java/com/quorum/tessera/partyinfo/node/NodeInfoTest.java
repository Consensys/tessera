package com.quorum.tessera.partyinfo.node;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.quorum.tessera.encryption.PublicKey;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class NodeInfoTest {

  @Test(expected = NullPointerException.class)
  public void createEmptyRequiresUrl() {
    NodeInfo.Builder.create().build();
  }

  @Test
  public void createWithOnlyUrl() {
    String url = "someurl";
    NodeInfo nodeInfo = NodeInfo.Builder.create().withUrl(url).build();

    assertThat(nodeInfo.getUrl()).isEqualTo(url);
    assertThat(nodeInfo.getRecipients()).isEmpty();
    assertThat(nodeInfo.supportedApiVersions()).isEmpty();
  }

  @Test
  public void createWithEverything() {
    String url = "someurl";

    final Recipient recipient = mock(Recipient.class);
    PublicKey publicKey = mock(PublicKey.class);
    when(recipient.getUrl()).thenReturn("http://someurl.com/");
    when(recipient.getKey()).thenReturn(publicKey);

    Collection<Recipient> recipients = List.of(recipient);
    Collection<String> supportedVersions = List.of("ONE", "TWO");

    NodeInfo nodeInfo =
        NodeInfo.Builder.create()
            .withUrl(url)
            .withRecipients(recipients)
            .withSupportedApiVersions(supportedVersions)
            .build();

    assertThat(nodeInfo.getUrl()).isEqualTo(url);
    assertThat(nodeInfo.getRecipients()).isEqualTo(Set.copyOf(recipients));

    assertThat(nodeInfo.getRecipientsAsMap())
        .containsKey(publicKey)
        .containsValue("http://someurl.com/");

    assertThat(nodeInfo.supportedApiVersions()).isEqualTo(Set.copyOf(supportedVersions));
  }
}
