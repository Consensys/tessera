
package com.quorum.tessera.partyinfo;

import java.util.UUID;


public interface RandomDataGenerator {
    
    default String generate() {
        return UUID.randomUUID().toString();
    }
}
