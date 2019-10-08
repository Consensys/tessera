
package com.quorum.tessera.partyinfo;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import java.util.Set;

public interface PartyInfoValidator {
    
    Set<Recipient> validateAndFetchValidRecipients(PartyInfo partyInfo,PartyInfoValidatorCallback partyInfoValidatorCallback);
    
    static PartyInfoValidator create(Enclave enclave) {
        return new PartyInfoValidatorImpl(enclave);
    }
    
}
