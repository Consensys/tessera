package com.quorum.tessera.partyinfo;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import static java.util.Collections.singleton;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

import static org.mockito.Mockito.*;


public class ResendBatchPublisherTest {

    
    @Test
    public void testPublishBatchWithKey() {

        String uri = "http://myurl.com";
        PartyInfoStore partyInfoStore = PartyInfoStore.create(URI.create("http://myurl.com"));
        PublicKey myKey = PublicKey.from("I LOVE SPARROWS".getBytes());
        Recipient recipient = new Recipient(myKey, "http://otherurl.com");

        PartyInfo partyInfo = new PartyInfo(uri, singleton(recipient), Collections.EMPTY_SET);
        partyInfoStore.store(partyInfo);
        
        
        List<String> results = new ArrayList<>();
        ResendBatchPublisher payloadPublisher = new ResendBatchPublisher() {
            @Override
            public void publishBatch(List<EncodedPayload> payloads, String targetUrl) {
                results.add(targetUrl);
            }
        };
        
        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        payloadPublisher.publishBatch(Arrays.asList(encodedPayload), myKey);
        
        assertThat(results).containsExactly("http://otherurl.com");
        
    }

}
