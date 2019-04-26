
package com.quorum.tessera.node;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.node.model.PartyInfo;
import com.quorum.tessera.node.model.Recipient;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;


public class PartyInfoRecipientUpdateCheckTest {
    
    
    @Test
    public void validateEmpty() {
        
        String url = "http://somedomain.com";
        
        Set<Recipient> existingRecipients = new HashSet<>();
        Set<Recipient> newRecipients = new HashSet<>();
        PartyInfo existingPartyInfo = new PartyInfo(url, existingRecipients, Collections.EMPTY_SET);
        PartyInfo newPartyInfo = new PartyInfo(url, newRecipients, Collections.EMPTY_SET);
        PartyInfoRecipientUpdateCheck check = new PartyInfoRecipientUpdateCheck(existingPartyInfo,newPartyInfo);
        
        assertThat(check.validateKeysToUrls()).isTrue();
        
    }
    
    @Test
    public void attemptToChangeUrl() {
        
        String url = "http://somedomain.com";
        
        PublicKey key = PublicKey.from("ONE".getBytes());
        
        Recipient existingRecipient = new Recipient(key,"http://one.com");
        Set<Recipient> existingRecipients = Collections.singleton(existingRecipient);
        
        Recipient newRecipient = new Recipient(key,"http://two.com");
        Set<Recipient> newRecipients = Collections.singleton(newRecipient);
        
        
        PartyInfo existingPartyInfo = new PartyInfo(url, existingRecipients, Collections.EMPTY_SET);
        PartyInfo newPartyInfo = new PartyInfo(url, newRecipients, Collections.EMPTY_SET);
        
        PartyInfoRecipientUpdateCheck check = new PartyInfoRecipientUpdateCheck(existingPartyInfo,newPartyInfo);
        assertThat(check.validateKeysToUrls()).isFalse();
        
    } 
}
