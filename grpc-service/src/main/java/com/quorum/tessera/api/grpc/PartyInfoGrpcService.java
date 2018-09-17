package com.quorum.tessera.api.grpc;

import com.google.protobuf.ByteString;
import com.quorum.tessera.api.grpc.model.PartyInfoMessage;
import com.quorum.tessera.node.PartyInfoParser;
import com.quorum.tessera.node.PartyInfoService;
import com.quorum.tessera.node.model.PartyInfo;
import io.grpc.stub.StreamObserver;

import static java.util.Objects.requireNonNull;

public class PartyInfoGrpcService extends PartyInfoGrpc.PartyInfoImplBase {

    private final PartyInfoParser partyInfoParser;

    private final PartyInfoService partyInfoService;

    public PartyInfoGrpcService(final PartyInfoService partyInfoService,
            final PartyInfoParser partyInfoParser) {
        this.partyInfoService = requireNonNull(partyInfoService, "partyInfoService must not be null");
        this.partyInfoParser = requireNonNull(partyInfoParser, "partyInfoParser must not be null");
    }

    @Override
    public void getPartyInfo(PartyInfoMessage request, StreamObserver<PartyInfoMessage> responseObserver) {
        doGetPartyInfo(request, responseObserver);
        responseObserver.onCompleted();
    }

    /**
     * Experiment
     *
     * @param request
     * @param responseObserver
     */
//    @Override
//    public StreamObserver<PartyInfoMessage> getPartyInfoStream(StreamObserver<PartyInfoMessage> responseObserver) {
//        return new StreamObserver<PartyInfoMessage>() {
//
//            @Override
//            public void onNext(PartyInfoMessage value) {
//                doGetPartyInfo(value, responseObserver);
//            }
//
//            @Override
//            public void onError(Throwable t) {
//
//            }
//
//            @Override
//            public void onCompleted() {
//                responseObserver.onCompleted();
//            }
//        };
//    }
    private void doGetPartyInfo(PartyInfoMessage request, StreamObserver<PartyInfoMessage> responseObserver) {

        final PartyInfo partyInfo = partyInfoParser.from(request.getPartyInfo().toByteArray());

        final PartyInfo updatedPartyInfo = partyInfoService.updatePartyInfo(partyInfo);

        final PartyInfoMessage response = PartyInfoMessage.newBuilder()
                .setPartyInfo(ByteString.copyFrom(partyInfoParser.to(updatedPartyInfo)))
                .build();

        responseObserver.onNext(response);
    }
}
