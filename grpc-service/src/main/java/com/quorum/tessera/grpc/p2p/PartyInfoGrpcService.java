package com.quorum.tessera.grpc.p2p;

import com.google.protobuf.ByteString;
import com.quorum.tessera.grpc.p2p.PartyInfoMessage;
import com.quorum.tessera.grpc.StreamObserverTemplate;
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

        StreamObserverTemplate template = new StreamObserverTemplate(responseObserver);

        template.handle(() -> {
            
            final PartyInfo partyInfo = partyInfoParser.from(request.getPartyInfo().toByteArray());

            final PartyInfo updatedPartyInfo = partyInfoService.updatePartyInfo(partyInfo);

            return PartyInfoMessage.newBuilder()
                    .setPartyInfo(ByteString.copyFrom(partyInfoParser.to(updatedPartyInfo)))
                    .build();

        });
    }

  
}
