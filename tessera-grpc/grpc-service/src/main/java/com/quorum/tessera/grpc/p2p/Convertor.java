package com.quorum.tessera.grpc.p2p;

import com.quorum.tessera.api.model.DeleteRequest;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class Convertor {

    private Convertor() {
        throw new UnsupportedOperationException("This object should not be constructed.");
    }

    public static com.quorum.tessera.grpc.p2p.ResendRequest toGrpc(com.quorum.tessera.api.model.ResendRequest request) {
        com.quorum.tessera.grpc.p2p.ResendRequestType resendRequestType =
                Optional.ofNullable(request.getType())
                        .filter(Objects::nonNull)
                        .map(v -> v.name())
                        .map(com.quorum.tessera.grpc.p2p.ResendRequestType::valueOf)
                        .orElse(com.quorum.tessera.grpc.p2p.ResendRequestType.INDIVIDUAL);

        return com.quorum.tessera.grpc.p2p.ResendRequest.newBuilder()
                .setKey(request.getKey())
                .setPublicKey(request.getPublicKey())
                .setType(resendRequestType)
                .build();
    }

    public static DeleteRequest toModel(com.quorum.tessera.grpc.p2p.DeleteRequest grpcRequest) {
        DeleteRequest request = new DeleteRequest();
        request.setKey(grpcRequest.getKey());

        return request;
    }

    public static com.quorum.tessera.api.model.ResendRequest toModel(
            com.quorum.tessera.grpc.p2p.ResendRequest grpcObject) {
        com.quorum.tessera.api.model.ResendRequest resendRequest = new com.quorum.tessera.api.model.ResendRequest();
        resendRequest.setKey(grpcObject.getKey());
        resendRequest.setPublicKey(grpcObject.getPublicKey());

        Stream.of(com.quorum.tessera.grpc.p2p.ResendRequestType.values())
                .map(Enum::name)
                .filter(n -> n.equals(grpcObject.getType().name()))
                .map(com.quorum.tessera.api.model.ResendRequestType::valueOf)
                .findAny()
                .ifPresent(resendRequest::setType);

        return resendRequest;
    }
}
