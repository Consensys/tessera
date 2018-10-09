package com.quorum.tessera.encryption;

import java.util.Base64;


public interface Key {
    
    byte[] getKeyBytes();
    
   default String encodeToBase64() {
       return Base64.getEncoder().encodeToString(getKeyBytes());
   }

}
