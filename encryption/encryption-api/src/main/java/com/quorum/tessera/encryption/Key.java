package com.quorum.tessera.encryption;


public interface Key {
    
    byte[] getKeyBytes();
    
    String encodeToBase64();

}
