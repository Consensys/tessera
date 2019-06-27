
package com.jpmorgan.quorum.tessera.sync;

import com.quorum.tessera.partyinfo.PartyInfoService;
import java.util.Objects;

public class PartyInfoServiceHolder {
    
    private static PartyInfoService partyInfoService;

    public PartyInfoServiceHolder(PartyInfoService p) {
        partyInfoService = Objects.requireNonNull(p, "PartyInfoService is required");
    }

    public static PartyInfoService getPartyInfoService() {
        if(Objects.isNull(partyInfoService)) {
            throw new IllegalStateException("PartyInfoServiceHolder has not been initialsied. ");
        }
        return partyInfoService;
    }
 
}
