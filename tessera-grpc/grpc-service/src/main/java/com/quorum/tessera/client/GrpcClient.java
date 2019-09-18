package com.quorum.tessera.client;

import com.quorum.tessera.grpc.p2p.*;

interface GrpcClient {

    byte[] sendPartyInfo(byte[] data);

    boolean makeResendRequest(ResendRequest grpcObj);

    byte[] push(byte[] data);
}
