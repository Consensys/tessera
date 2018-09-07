package com.quorum.tessera.client;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.node.PostDelegate;
import java.util.Objects;
import java.util.ServiceLoader;


public class P2pClientFactory {
    
    private final PostDelegate postDelegate;
        
    private final CommunicationType communicationType;

    public P2pClientFactory(PostDelegate postDelegate, 
            CommunicationType communicationType) {
        this.postDelegate = postDelegate;
        this.communicationType = Objects.requireNonNull(communicationType);
    }
    
    public P2pClient create() {
        if(communicationType == CommunicationType.REST) {
            return new RestP2pClient(postDelegate);
        } 
        
        return  ServiceLoader.load(P2pClient.class).iterator().next();

    }
    
    
    
    
}
