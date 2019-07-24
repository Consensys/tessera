
package com.quorum.tessera.partyinfo;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;


public interface PayloadPublisherFactory {
    
    PayloadPublisher create(Config config);

    CommunicationType communicationType();
    
    static PayloadPublisherFactory newFactory(Config config) {
        
        ServerConfig serverConfig = config.getP2PServerConfig();
        List<PayloadPublisherFactory> factories = new ArrayList<>();
        Iterator<PayloadPublisherFactory> it = ServiceLoader.load(PayloadPublisherFactory.class).iterator();
        while(it.hasNext()) {
            factories.add(it.next());
        }
        return factories.stream()
                .filter(f -> f.communicationType() == serverConfig.getCommunicationType())
                .findAny()
                .orElseThrow(() -> new UnsupportedOperationException("Unable to create a PayloadPublisherFactory for"+ serverConfig.getCommunicationType()));
        
    }
    
}
