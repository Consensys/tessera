
package com.quorum.tessera.partyinfo;

import com.quorum.tessera.partyinfo.model.Recipient;

@FunctionalInterface
public interface PartyInfoValidatorCallback {
    
    String requestDecode(Recipient recipient,byte[] encodedPayloadData);

}
