package com.quorum.tessera.encryption;

import java.io.Serializable;


public interface Key extends Serializable {
    
    byte[] getKeyBytes();
    
    String encodeToBase64();

}
