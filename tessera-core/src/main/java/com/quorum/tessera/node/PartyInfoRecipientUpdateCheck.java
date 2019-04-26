
package com.quorum.tessera.node;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.node.model.PartyInfo;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PartyInfoRecipientUpdateCheck {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoRecipientUpdateCheck.class);
    
    private final PartyInfo existingPartyInfo;
    
    private final PartyInfo newPartyInfo;

    public PartyInfoRecipientUpdateCheck(PartyInfo existingPartyInfo, PartyInfo newPartyInfo) {
        this.existingPartyInfo = Objects.requireNonNull(existingPartyInfo);
        this.newPartyInfo = Objects.requireNonNull(newPartyInfo);
    }

    public boolean validateKeysToUrls() {

        final Map<PublicKey, String> existingRecipientKeyUrlMap = existingPartyInfo.getRecipients()
            .stream()
            .collect(Collectors.toMap(r -> r.getKey(), r -> r.getUrl()));

        final Map<PublicKey, String> newRecipientKeyUrlMap = newPartyInfo.getRecipients()
            .stream()
            .collect(Collectors.toMap(r -> r.getKey(), r -> r.getUrl()));

        for(Map.Entry<PublicKey,String> e : newRecipientKeyUrlMap.entrySet()) {
            PublicKey key = e.getKey();
            if(existingRecipientKeyUrlMap.containsKey(key)) {
                
                String existingUrl = existingRecipientKeyUrlMap.get(key);
                String newUrl = newRecipientKeyUrlMap.get(key);
                if(!existingUrl.equalsIgnoreCase(newUrl)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    
}
