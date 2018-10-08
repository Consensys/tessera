
package com.quorum.tessera.key;

import java.util.Base64;

public interface KeyUtil {
    
    static String encodeToBase64(PublicKey key) {
        return Base64.getEncoder().encodeToString(key.getKeyBytes());
    }
    
    static String encodeToBase64(PrivateKey key) {
        return Base64.getEncoder().encodeToString(key.getKeyBytes());
    }
}
