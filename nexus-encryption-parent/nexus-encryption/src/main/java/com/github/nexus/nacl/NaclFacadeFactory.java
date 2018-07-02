package com.github.nexus.nacl;

import java.util.ServiceLoader;

public interface NaclFacadeFactory {
    
    NaclFacade create();
    
    
    static NaclFacadeFactory newFactory() {
         ServiceLoader<NaclFacadeFactory> serviceLoader = ServiceLoader.load(NaclFacadeFactory.class);
         return serviceLoader.iterator().next();
    } 
    
}
