
package com.quorum.tessera.p2p;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import com.quorum.tessera.partyinfo.ResendBatchPublisher;
import com.quorum.tessera.partyinfo.ResendBatchPublisherFactory;
import javax.ws.rs.client.Client;


public class RestResendBatchPublisherFactory implements ResendBatchPublisherFactory {

    @Override
    public ResendBatchPublisher create(Config config) {
        ClientFactory clientFactory = new ClientFactory();
        Client client = clientFactory.buildFrom(config.getP2PServerConfig());
        return new RestResendBatchPublisher(client);
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.REST;
    }
    
}
