
package com.quorum.tessera.config.builder;

import com.quorum.tessera.config.SslTrustMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public interface SslTrustModeFactory {
    
     Map<String, SslTrustMode> TRUST_MODE_LOOKUP = 
             Collections.unmodifiableMap(new HashMap<String, SslTrustMode>() {
        {
            put("ca", SslTrustMode.CA);
            put("tofu", SslTrustMode.TOFU);
            put("ca-or-tofu", SslTrustMode.CA_OR_TOFU);
            put("whitelist", SslTrustMode.WHITELIST);
            put("insecure-no-validation",SslTrustMode.NONE);
            put("none", SslTrustMode.NONE);
        }
    });
    
    static SslTrustMode resolveByLegacyValue(String value) {
        return TRUST_MODE_LOOKUP.getOrDefault(value, SslTrustMode.NONE);
    }
    
    
}
