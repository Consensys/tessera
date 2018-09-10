package com.quorum.tessera.api.grpc;

import com.quorum.tessera.api.grpc.model.SendResponse;
import com.quorum.tessera.api.model.DeleteRequest;
import com.quorum.tessera.api.model.ReceiveRequest;
import com.quorum.tessera.api.model.ResendRequest;
import com.quorum.tessera.api.model.ResendRequestType;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class Convertor {

    public static com.quorum.tessera.api.grpc.model.ResendRequest toGrpc(ResendRequest request) {
        com.quorum.tessera.api.grpc.model.ResendRequestType resendRequestType = 
                Optional.ofNullable(request.getType())
                        .filter(Objects::nonNull)
                        .map(v -> v.name())
                        .map(com.quorum.tessera.api.grpc.model.ResendRequestType::valueOf)
                        .orElse(com.quorum.tessera.api.grpc.model.ResendRequestType.INDIVIDUAL);
        
        return com.quorum.tessera.api.grpc.model.ResendRequest.newBuilder()
                .setKey(request.getKey()).setPublicKey(request.getPublicKey())
                .setType(resendRequestType).build();
    }

    private Convertor() {
        throw new UnsupportedOperationException("This object should not be constructed.");
    }

    public static DeleteRequest toModel(com.quorum.tessera.api.grpc.model.DeleteRequest grpcRequest) {
        DeleteRequest request = new DeleteRequest();
        request.setKey(grpcRequest.getKey());

        return request;
    }

    public static com.quorum.tessera.api.model.SendRequest toModel(com.quorum.tessera.api.grpc.model.SendRequest grpcObject) {
        com.quorum.tessera.api.model.SendRequest sendRequest = new com.quorum.tessera.api.model.SendRequest();
        sendRequest.setTo(grpcObject.getToList().toArray(new String[0]));
        sendRequest.setFrom(grpcObject.getFrom());
        sendRequest.setPayload(grpcObject.getPayload());
        return sendRequest;
    }

    public static com.quorum.tessera.api.grpc.model.SendRequest toGrpc(com.quorum.tessera.api.model.SendRequest modelObject) {
        return com.quorum.tessera.api.grpc.model.SendRequest.newBuilder()
                .setFrom(modelObject.getFrom())
                .setPayload(modelObject.getPayload())
                .addAllTo(Arrays.asList(modelObject.getTo()))
                .build();
    }

    public static ReceiveRequest toModel(com.quorum.tessera.api.grpc.model.ReceiveRequest grpcObject) {
        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(grpcObject.getKey());
        receiveRequest.setTo(grpcObject.getTo());

        return receiveRequest;
    }

    public static com.quorum.tessera.api.grpc.model.ReceiveRequest toGrpc(ReceiveRequest modelObject) {
        return com.quorum.tessera.api.grpc.model.ReceiveRequest.newBuilder()
                .setKey(modelObject.getKey())
                .setTo(modelObject.getTo())
                .build();
    }

    public static com.quorum.tessera.api.model.ResendRequest toModel(com.quorum.tessera.api.grpc.model.ResendRequest grpcObject) {
        com.quorum.tessera.api.model.ResendRequest resendRequest = new ResendRequest();
        resendRequest.setKey(grpcObject.getKey());
        resendRequest.setPublicKey(grpcObject.getPublicKey());

        Stream.of(com.quorum.tessera.api.grpc.model.ResendRequestType.values())
                .map(Enum::name)
                .filter(n -> n.equals(grpcObject.getType().name()))
                .map(ResendRequestType::valueOf)
                .findAny().ifPresent(resendRequest::setType);

        return resendRequest;
    }

    public static SendResponse toGrpc(com.quorum.tessera.api.model.SendResponse response) {
        return SendResponse.newBuilder()
                .setKey(response.getKey())
                .build();
    }

}
