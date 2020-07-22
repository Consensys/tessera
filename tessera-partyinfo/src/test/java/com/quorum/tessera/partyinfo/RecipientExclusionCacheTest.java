package com.quorum.tessera.partyinfo;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.model.Recipient;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class RecipientExclusionCacheTest {

    private RecipientExclusionCache exclusionCache;

    @Before
    public void onSetup() {
        exclusionCache = new RecipientExclusionCache(200,200, TimeUnit.MILLISECONDS);
    }

    @After
    public void onTearDown() {
        exclusionCache.stop();
    }

    @Test
    public void excludeRecipient() throws Exception {

        PublicKey publicKey = PublicKey.from("SOMEDATA".getBytes());
        String url = "http://someurl.com";
        Recipient recipient = Recipient.of(publicKey,url);

        exclusionCache.exclude(recipient);

        assertThat(exclusionCache.isExcluded(recipient)).isTrue();

        exclusionCache.start();

        TimeUnit.MILLISECONDS.sleep(1000);

        assertThat(exclusionCache.isExcluded(recipient)).isFalse();

    }

    @Test
    public void defaultConstructor() {
        assertThat(new RecipientExclusionCache()).isNotNull();

    }

    @Test
    public void expiryKeyEqualsAndHashCode() {
        EqualsVerifier.forClass(RecipientExclusionCache.ExpiryKey.class)
            .usingGetClass().withIgnoredFields("expiry").verify();
    }



}
