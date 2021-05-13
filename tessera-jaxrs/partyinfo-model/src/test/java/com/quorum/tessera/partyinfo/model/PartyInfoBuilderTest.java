package com.quorum.tessera.partyinfo.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.quorum.tessera.encryption.PublicKey;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

public class PartyInfoBuilderTest {

  private PartyInfoBuilder partyInfoBuilder;

  @Before
  public void beforeTest() {
    partyInfoBuilder = PartyInfoBuilder.create();
  }

  @Test
  public void buildOnlyWithUrl() {

    final String url = "http://pinfo.com";

    PartyInfo partyInfo = partyInfoBuilder.withUri(url).build();

    assertThat(partyInfo).isNotNull();
    assertThat(partyInfo.getUrl()).isEqualTo(url.concat("/"));
    assertThat(partyInfo.getRecipients()).isEmpty();
    assertThat(partyInfo.getParties()).isEmpty();
  }

  @Test
  public void buildWithRecipientsAndFilterKeysWeDontOwn() {

    final String url = "http://pinfo.com";
    PublicKey ownKey = mock(PublicKey.class);
    PublicKey publicKey = mock(PublicKey.class);
    PartyInfo partyInfo =
        partyInfoBuilder
            .withUri(url)
            .withRecipients(Map.of(publicKey, "http://bobbysixkiller.com", ownKey, url))
            .build();

    assertThat(partyInfo).isNotNull();
    assertThat(partyInfo.getUrl()).isEqualTo(url.concat("/"));
    assertThat(partyInfo.getRecipients()).hasSize(1);
    assertThat(partyInfo.getParties()).hasSize(2);

    Recipient recipient = partyInfo.getRecipients().iterator().next();
    assertThat(recipient.getKey()).isSameAs(ownKey);
    assertThat(recipient.getUrl()).isEqualTo("http://pinfo.com/");

    Set<Party> partySet = partyInfo.getParties();
    assertThat(partySet)
        .containsExactlyInAnyOrder(
            new Party("http://pinfo.com"), new Party("http://bobbysixkiller.com"));
  }
}
