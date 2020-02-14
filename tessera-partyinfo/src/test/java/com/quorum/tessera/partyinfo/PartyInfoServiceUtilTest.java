package com.quorum.tessera.partyinfo;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class PartyInfoServiceUtilTest {

    @Test
    public void validateEmptyRecipientListsAsValid() {
        final String url = "http://somedomain.com";

        final Set<Recipient> existingRecipients = new HashSet<>();
        final Set<Recipient> newRecipients = new HashSet<>();
        final PartyInfo existingPartyInfo = new PartyInfo(url, existingRecipients, Collections.emptySet());
        final PartyInfo newPartyInfo = new PartyInfo(url, newRecipients, Collections.emptySet());

        assertThat(PartyInfoServiceUtil.validateKeysToUrls(existingPartyInfo, newPartyInfo)).isTrue();
    }

    @Test
    public void validateSameRecipientListsAsValid() {
        final String url = "http://somedomain.com";
        final PublicKey key = PublicKey.from("ONE".getBytes());

        final Set<Recipient> existingRecipients = Collections.singleton(new Recipient(key, "http://one.com"));
        final PartyInfo existingPartyInfo = new PartyInfo(url, existingRecipients, Collections.emptySet());

        final Set<Recipient> newRecipients = Collections.singleton(new Recipient(key, "http://one.com"));
        final PartyInfo newPartyInfo = new PartyInfo(url, newRecipients, Collections.emptySet());

        assertThat(PartyInfoServiceUtil.validateKeysToUrls(existingPartyInfo, newPartyInfo)).isTrue();
    }

    @Test
    public void validateAttemptToChangeUrlAsInvalid() {
        final String url = "http://somedomain.com";
        final PublicKey key = PublicKey.from("ONE".getBytes());

        final Set<Recipient> existingRecipients = Collections.singleton(new Recipient(key, "http://one.com"));
        final PartyInfo existingPartyInfo = new PartyInfo(url, existingRecipients, Collections.emptySet());

        final Set<Recipient> newRecipients = Collections.singleton(new Recipient(key, "http://two.com"));
        final PartyInfo newPartyInfo = new PartyInfo(url, newRecipients, Collections.emptySet());

        assertThat(PartyInfoServiceUtil.validateKeysToUrls(existingPartyInfo, newPartyInfo)).isFalse();
    }
}
