
package com.quorum.tessera.client;

import com.quorum.tessera.api.grpc.model.ResendRequest;


interface GrpcClient {

    byte[] getPartyInfo(byte[] data);

    boolean makeResendRequest(ResendRequest grpcObj);

    byte[] push(byte[] data);
    
}
