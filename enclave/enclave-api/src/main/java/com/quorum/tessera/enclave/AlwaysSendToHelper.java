package com.quorum.tessera.enclave;

import java.util.Collections;
import java.util.List;

@Deprecated
/**
 * 
 * Helper class for working around spring bean initialisation. 
 * 
 * TODO: The issue needs addressing rather than using this object
 */
public class AlwaysSendToHelper {
    
    private final List<String> alwaysSendTo;

    public AlwaysSendToHelper(List<String> alwaysSendTo) {
        this.alwaysSendTo = alwaysSendTo;
    }

    public List<String> getAlwaysSendTo() {
        return alwaysSendTo != null ? alwaysSendTo : Collections.emptyList();
    }
    
    
    
}
