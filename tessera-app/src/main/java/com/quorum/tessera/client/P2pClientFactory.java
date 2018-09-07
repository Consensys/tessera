package com.quorum.tessera.client;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.node.PostDelegate;
import com.quorum.tessera.node.grpc.GrpcClientFactory;
import java.util.Objects;


public class P2pClientFactory {
    
    private final PostDelegate postDelegate;
        
    private final CommunicationType communicationType;

    public P2pClientFactory(PostDelegate postDelegate, 
            CommunicationType communicationType) {
        this.postDelegate = postDelegate;
        this.communicationType = Objects.requireNonNull(communicationType);
    }
    
    public P2pClient create() {
        if(communicationType == CommunicationType.GRPC) {
            return new GrpcP2pClient(new GrpcClientFactory());
        } else {
            return new RestP2pClient(postDelegate);
        }
    }
    
    
    
    
}
