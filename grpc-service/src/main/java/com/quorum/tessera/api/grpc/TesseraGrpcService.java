package com.quorum.tessera.api.grpc;

import com.google.protobuf.Empty;
import com.quorum.tessera.api.grpc.model.UpCheckMessage;
import com.quorum.tessera.api.grpc.model.VersionMessage;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TesseraGrpcService extends TesseraGrpc.TesseraImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(TesseraGrpcService.class);

    private static final String UPCHECK_RESPONSE = "I'm up!";

    private static final String VERSION = "0.6";

    @Override
    public void getVersion(Empty request, StreamObserver<VersionMessage> responseObserver) {
        LOGGER.info("GET version");

        StreamObserverTemplate template = new StreamObserverTemplate(responseObserver);

        template.handle(() -> {
            return VersionMessage.newBuilder()
                    .setVersion(VERSION)
                    .build();
        });

    }

    @Override
    public void getUpCheck(Empty request, StreamObserver<UpCheckMessage> responseObserver) {
        LOGGER.info("GET upcheck");
        StreamObserverTemplate template = new StreamObserverTemplate(responseObserver);

        template.handle(() -> {
            return UpCheckMessage.newBuilder()
                    .setUpCheck(UPCHECK_RESPONSE)
                    .build();
        });

    }

}
