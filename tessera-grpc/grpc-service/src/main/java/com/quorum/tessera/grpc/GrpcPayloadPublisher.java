
package com.quorum.tessera.grpc;

import com.quorum.tessera.client.GrpcP2pClient;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.partyinfo.PayloadPublisher;
import java.util.Objects;


public class GrpcPayloadPublisher implements PayloadPublisher {
    
    private final com.quorum.tessera.client.GrpcP2pClient p2pClient;

    private final PayloadEncoder payloadEncoder;

    protected GrpcPayloadPublisher(GrpcP2pClient p2pClient, PayloadEncoder payloadEncoder) {
        this.p2pClient = Objects.requireNonNull(p2pClient);
        this.payloadEncoder = Objects.requireNonNull(payloadEncoder);
    }

    public GrpcPayloadPublisher() {
        this(new GrpcP2pClient(),PayloadEncoder.create());
    }

    @Override
    public void publishPayload(EncodedPayload payload, String targetUrl) {
        p2pClient.push(targetUrl, payloadEncoder.encode(payload));
    }
 
}
