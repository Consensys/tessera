
package com.quorum.tessera.grpc.p2p;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.partyinfo.ResendBatchPublisher;
import com.quorum.tessera.partyinfo.ResendBatchPublisherFactory;


public class GrpcResendBatchPublisherFactory implements ResendBatchPublisherFactory {

    @Override
    public ResendBatchPublisher create(Config config) {
        return new GrpcResendBatchPublisher();
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.GRPC;
    }
    
}
