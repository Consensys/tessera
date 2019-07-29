
package com.quorum.tessera.grpc;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.partyinfo.PayloadPublisher;
import com.quorum.tessera.partyinfo.PayloadPublisherFactory;


public class GrpcPayloadPublisherFactory implements PayloadPublisherFactory {

    @Override
    public PayloadPublisher create(Config config) {
        return new GrpcPayloadPublisher();
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.GRPC;
    }
    
}
