package com.quorum.tessera.grpc.p2p;

import com.google.protobuf.ByteString;
import com.quorum.tessera.api.model.DeleteRequest;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
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

    public static com.quorum.tessera.partyinfo.ResendBatchRequest toModel(
            com.quorum.tessera.grpc.p2p.ResendBatchRequest grpcRequest) {
        com.quorum.tessera.partyinfo.ResendBatchRequest request = new com.quorum.tessera.partyinfo.ResendBatchRequest();
        request.setBatchSize(grpcRequest.getBatchSize());
        request.setPublicKey(grpcRequest.getPublicKey());
        return request;
    }

    public static com.quorum.tessera.grpc.p2p.ResendBatchRequest toGrpc(
            com.quorum.tessera.partyinfo.ResendBatchRequest request) {
        return com.quorum.tessera.grpc.p2p.ResendBatchRequest.newBuilder()
                .setBatchSize(request.getBatchSize())
                .setPublicKey(request.getPublicKey())
                .build();
    }

    public static com.quorum.tessera.partyinfo.ResendBatchResponse toModel(
            com.quorum.tessera.grpc.p2p.ResendBatchResponse grpcRequest) {
        com.quorum.tessera.partyinfo.ResendBatchResponse response =
                new com.quorum.tessera.partyinfo.ResendBatchResponse();
        response.setTotal(grpcRequest.getTotal());
        return response;
    }

    public static com.quorum.tessera.grpc.p2p.ResendBatchResponse toGrpc(
            com.quorum.tessera.partyinfo.ResendBatchResponse response) {
        return com.quorum.tessera.grpc.p2p.ResendBatchResponse.newBuilder().setTotal(response.getTotal()).build();
    }

    public static com.quorum.tessera.partyinfo.PushBatchRequest toModel(
            com.quorum.tessera.grpc.p2p.PushBatchRequest grpcRequest) {
        final List<byte[]> payloads =
                grpcRequest.getDataList().stream().map(ByteString::toByteArray).collect(Collectors.toList());
        return new com.quorum.tessera.partyinfo.PushBatchRequest(payloads);
    }

    public static com.quorum.tessera.grpc.p2p.PushBatchRequest toGrpc(
            com.quorum.tessera.partyinfo.PushBatchRequest request) {
        return com.quorum.tessera.grpc.p2p.PushBatchRequest.newBuilder()
                .addAllData(
                        request.getEncodedPayloads().stream().map(ByteString::copyFrom).collect(Collectors.toList()))
                .build();
    }
}
