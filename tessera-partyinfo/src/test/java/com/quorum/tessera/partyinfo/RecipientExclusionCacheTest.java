package com.quorum.tessera.partyinfo;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.model.Recipient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class RecipientExclusionCacheTest {

    private RecipientExclusionCache exclusionCache;

    @Before
    public void onSetup() {
        exclusionCache = new RecipientExclusionCache();
    }

    @After
    public void onTearDown() {

    }

    @Test
    public void excludeRecipient() throws Exception {

        PublicKey publicKey = PublicKey.from("SOMEDATA".getBytes());
        String url = "http://someurl.com";
        Recipient recipient = Recipient.of(publicKey,url);

        exclusionCache.exclude(recipient);
        assertThat(exclusionCache.isExcluded(recipient)).isTrue();

    }

    @Test
    public void includeNonExcludedRecipient() throws Exception {

        PublicKey publicKey = PublicKey.from("SOMEDATA".getBytes());
        String url = "http://someurl.com";
        Recipient recipient = Recipient.of(publicKey,url);

        Optional<Recipient> result = exclusionCache.include(url);
        assertThat(result).isNotPresent();

    }

    @Test
    public void excludeAndThenIncludeRecipient() throws Exception {

        PublicKey publicKey = PublicKey.from("SOMEDATA".getBytes());
        String url = "http://someurl.com";
        Recipient recipient = Recipient.of(publicKey,url);

        assertThat(exclusionCache.include(url)).isNotPresent();

        exclusionCache.exclude(recipient);

        Optional<Recipient> result = exclusionCache.include(url);
        assertThat(result).isPresent().containsSame(recipient);

    }

}
