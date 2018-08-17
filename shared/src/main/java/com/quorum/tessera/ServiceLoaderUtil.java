package com.quorum.tessera;

import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;

public interface ServiceLoaderUtil {
    
    static <T> Optional<T> load(Class<T> type) {
        Iterator<T> it =  ServiceLoader.load(type).iterator();
        if(it.hasNext()) {
            return Optional.of(it.next());
        }
        return Optional.empty();
    }
    
}
