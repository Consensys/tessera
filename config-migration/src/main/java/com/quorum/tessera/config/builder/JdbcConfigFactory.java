
package com.quorum.tessera.config.builder;

import com.quorum.tessera.config.JdbcConfig;
import java.util.Optional;


public interface JdbcConfigFactory {

    static JdbcConfig fromLegacyStorageString(String storage) {
        
        Optional.ofNullable(storage)
                .orElseThrow(IllegalArgumentException::new);
        
        if(storage.startsWith("jdbc")) {
            return new JdbcConfig(null, null, storage, null);
        }

        if(storage.startsWith("sqlite")) {
            return new JdbcConfig(null, null, String.format("jdbc:%s", storage), null);
        }
        
        if(storage.startsWith("memory")) {
            return new JdbcConfig(null, null, "jdbc:h2:mem:tessera", null);
        }

        throw new UnsupportedOperationException(String.format("%s is not a supported storage option.", storage));
        
        
        
    }
    
    
    

    
}
