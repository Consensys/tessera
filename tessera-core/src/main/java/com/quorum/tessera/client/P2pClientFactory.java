
package com.quorum.tessera.client;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;



public interface P2pClientFactory {
    
    P2pClient create(Config config);
    
    CommunicationType communicationType();
    
    
    static  P2pClientFactory newFactory(Config config) {
        List<P2pClientFactory> all = new ArrayList<>();
        ServiceLoader.load(P2pClientFactory.class).forEach(all::add);
        return all.stream()
                .filter(c -> c.communicationType() == config.getServerConfig().getCommunicationType())
                .findFirst().get();
    }
    
}
