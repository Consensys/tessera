package com.quorum.tessera.partyinfo;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Recipient;
import java.util.Collections;
import java.util.Set;
import org.junit.Test;

public class PartyInfoServiceUtilTest {

  @Test
  public void validateEmptyRecipientListsAsValid() {
    final String url = "http://somedomain.com";

    final Set<Recipient> newRecipients = Set.of();

    final NodeInfo existingPartyInfo =
        NodeInfo.Builder.create().withRecipients(newRecipients).withUrl(url).build();

    final NodeInfo newPartyInfo =
        NodeInfo.Builder.create().withUrl(url).withRecipients(newRecipients).build();

    assertThat(PartyInfoServiceUtil.validateKeysToUrls(existingPartyInfo, newPartyInfo)).isTrue();
  }

  @Test
  public void validateSameRecipientListsAsValid() {
    final String url = "http://somedomain.com";
    final PublicKey key = PublicKey.from("ONE".getBytes());

    final Set<Recipient> existingRecipients =
        Collections.singleton(Recipient.of(key, "http://one.com"));
    final NodeInfo existingPartyInfo =
        NodeInfo.Builder.create().withUrl(url).withRecipients(existingRecipients).build();

    final Set<Recipient> newRecipients = Collections.singleton(Recipient.of(key, "http://one.com"));
    final NodeInfo newPartyInfo =
        NodeInfo.Builder.create().withUrl(url).withRecipients(newRecipients).build();

    assertThat(PartyInfoServiceUtil.validateKeysToUrls(existingPartyInfo, newPartyInfo)).isTrue();
  }

  @Test
  public void validateAttemptToChangeUrlAsInvalid() {
    final String url = "http://somedomain.com";
    final PublicKey key = PublicKey.from("ONE".getBytes());

    final Set<Recipient> existingRecipients =
        Collections.singleton(Recipient.of(key, "http://one.com"));
    final NodeInfo existingPartyInfo =
        NodeInfo.Builder.create().withUrl(url).withRecipients(existingRecipients).build();

    final Set<Recipient> newRecipients = Collections.singleton(Recipient.of(key, "http://two.com"));
    final NodeInfo newPartyInfo =
        NodeInfo.Builder.create().withUrl(url).withRecipients(newRecipients).build();

    assertThat(PartyInfoServiceUtil.validateKeysToUrls(existingPartyInfo, newPartyInfo)).isFalse();
  }
}
