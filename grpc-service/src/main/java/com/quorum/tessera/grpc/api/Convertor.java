package com.quorum.tessera.grpc.api;

import com.google.protobuf.ByteString;
import com.quorum.tessera.api.model.ReceiveRequest;

import java.util.Arrays;

public class Convertor {
    private Convertor() {
        throw new UnsupportedOperationException("This object should not be constructed.");
    }

    public static com.quorum.tessera.api.model.SendRequest toModel(com.quorum.tessera.grpc.api.SendRequest grpcObject) {
        com.quorum.tessera.api.model.SendRequest sendRequest = new com.quorum.tessera.api.model.SendRequest();
        sendRequest.setTo(grpcObject.getToList().toArray(new String[0]));
        if (!grpcObject.getFrom().isEmpty()) {
            sendRequest.setFrom(grpcObject.getFrom());
        }
        sendRequest.setPayload(grpcObject.getPayload().toByteArray());
        return sendRequest;
    }

    public static com.quorum.tessera.grpc.api.SendRequest toGrpc(com.quorum.tessera.api.model.SendRequest modelObject) {
        return com.quorum.tessera.grpc.api.SendRequest.newBuilder()
                .setFrom(modelObject.getFrom())
                .setPayload(ByteString.copyFrom(modelObject.getPayload()))
                .addAllTo(Arrays.asList(modelObject.getTo()))
                .build();
    }

    public static ReceiveRequest toModel(com.quorum.tessera.grpc.api.ReceiveRequest grpcObject) {
        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(grpcObject.getKey());
        receiveRequest.setTo(grpcObject.getTo());

        return receiveRequest;
    }

    public static com.quorum.tessera.grpc.api.ReceiveRequest toGrpc(ReceiveRequest modelObject) {
        return com.quorum.tessera.grpc.api.ReceiveRequest.newBuilder()
                .setKey(modelObject.getKey())
                .setTo(modelObject.getTo())
                .build();
    }

    public static SendResponse toGrpc(com.quorum.tessera.api.model.SendResponse response) {
        return SendResponse.newBuilder().setKey(response.getKey()).build();
    }
}
