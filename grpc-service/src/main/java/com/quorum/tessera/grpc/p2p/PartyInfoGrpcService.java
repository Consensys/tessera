package com.quorum.tessera.grpc.p2p;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import com.quorum.tessera.grpc.StreamObserverTemplate;
import com.quorum.tessera.partyinfo.PartyInfoParser;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import io.grpc.stub.StreamObserver;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

public class PartyInfoGrpcService extends PartyInfoGrpc.PartyInfoImplBase {

    private final PartyInfoParser partyInfoParser;

    private final PartyInfoService partyInfoService;

    public PartyInfoGrpcService(final PartyInfoService partyInfoService, final PartyInfoParser partyInfoParser) {
        this.partyInfoService = requireNonNull(partyInfoService, "partyInfoService must not be null");
        this.partyInfoParser = requireNonNull(partyInfoParser, "partyInfoParser must not be null");
    }

    @Override
    public void getPartyInfo(final PartyInfoMessage request, final StreamObserver<PartyInfoMessage> responseObserver) {

        final StreamObserverTemplate template = new StreamObserverTemplate(responseObserver);

        template.handle(() -> {
            
            final PartyInfo partyInfo = partyInfoParser.from(request.getPartyInfo().toByteArray());

            final PartyInfo updatedPartyInfo = partyInfoService.updatePartyInfo(partyInfo);

            return PartyInfoMessage.newBuilder()
                    .setPartyInfo(ByteString.copyFrom(partyInfoParser.to(updatedPartyInfo)))
                    .build();

        });
    }

    @Override
    public void getPartyInfoMessage(final Empty request, final StreamObserver<PartyInfoJson> responseObserver) {

        final StreamObserverTemplate template = new StreamObserverTemplate(responseObserver);

        template.handle(() -> {

            final PartyInfo partyInfo = partyInfoService.getPartyInfo();
            final Set<Peer> peers = partyInfo.getParties()
                .stream()
                .filter(p -> p.getUrl().endsWith("/"))
                .map(party -> {
                    final Peer.Builder builder = Peer.newBuilder().setUrl(party.getUrl());

                    if (party.getLastContacted() != null) {
                        final Timestamp timestamp = Timestamp.newBuilder()
                            .setSeconds(party.getLastContacted().getEpochSecond())
                            .build();

                        builder.setUtcTimestamp(timestamp);
                    }

                    return builder.build();
                }).collect(Collectors.toSet());

            final Map<String, String> keys = partyInfo.getRecipients()
                .stream()
                .collect(toMap(r -> r.getKey().encodeToBase64(), Recipient::getUrl));

            return PartyInfoJson.newBuilder()
                .setUrl(partyInfo.getUrl())
                .addAllPeers(peers)
                .putAllKeys(keys)
                .build();

        });
    }

}
