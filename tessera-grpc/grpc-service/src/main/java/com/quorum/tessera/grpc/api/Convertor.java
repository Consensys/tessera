package com.quorum.tessera.grpc.api;

import com.google.protobuf.ByteString;
import com.quorum.tessera.api.model.ReceiveRequest;
import com.quorum.tessera.api.model.SendSignedRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

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
        if (!grpcObject.getAffectedContractTransactionsList().isEmpty()) {
            sendRequest.setAffectedContractTransactions(
                    grpcObject.getAffectedContractTransactionsList().toArray(new String[0]));
        }
        if (!grpcObject.getExecHash().isEmpty()) {
            sendRequest.setExecHash(grpcObject.getExecHash());
        }
        sendRequest.setPrivacyFlag(grpcObject.getPrivacyFlagValue());
        sendRequest.setPayload(grpcObject.getPayload().toByteArray());
        return sendRequest;
    }

    public static SendSignedRequest toModel(com.quorum.tessera.grpc.api.SendSignedRequest grpcObject) {
        com.quorum.tessera.api.model.SendSignedRequest sendSignedRequest =
                new com.quorum.tessera.api.model.SendSignedRequest();
        sendSignedRequest.setTo(grpcObject.getToList().toArray(new String[0]));
        if (!grpcObject.getAffectedContractTransactionsList().isEmpty()) {
            sendSignedRequest.setAffectedContractTransactions(
                    grpcObject.getAffectedContractTransactionsList().toArray(new String[0]));
        }
        if (!grpcObject.getExecHash().isEmpty()) {
            sendSignedRequest.setExecHash(grpcObject.getExecHash());
        }
        sendSignedRequest.setPrivacyFlag(grpcObject.getPrivacyFlagValue());
        sendSignedRequest.setHash(grpcObject.getHash().toByteArray());
        return sendSignedRequest;
    }

    public static com.quorum.tessera.grpc.api.SendRequest toGrpc(com.quorum.tessera.api.model.SendRequest modelObject) {
        com.quorum.tessera.grpc.api.SendRequest.Builder builder = com.quorum.tessera.grpc.api.SendRequest.newBuilder();

        builder.setFrom(modelObject.getFrom())
                .setPayload(ByteString.copyFrom(modelObject.getPayload()))
                .addAllTo(Arrays.asList(modelObject.getTo()));
        if (modelObject.getAffectedContractTransactions() != null) {
            builder.addAllAffectedContractTransactions(Arrays.asList(modelObject.getAffectedContractTransactions()));
        }
        if (modelObject.getExecHash() != null) {
            builder.setExecHash(modelObject.getExecHash());
        }
        builder.setPrivacyFlagValue(modelObject.getPrivacyFlag());

        return builder.build();
    }

    public static com.quorum.tessera.grpc.api.SendSignedRequest toGrpc(
            com.quorum.tessera.api.model.SendSignedRequest modelObject) {
        return com.quorum.tessera.grpc.api.SendSignedRequest.newBuilder()
                .setHash(ByteString.copyFrom(modelObject.getHash()))
                .addAllTo(Arrays.asList(modelObject.getTo()))
                .addAllAffectedContractTransactions(
                        Optional.of(modelObject.getAffectedContractTransactions())
                                .map(Arrays::asList)
                                .orElse(Collections.emptyList()))
                .setExecHash(modelObject.getExecHash())
                .build();
    }

    public static ReceiveRequest toModel(com.quorum.tessera.grpc.api.ReceiveRequest grpcObject) {
        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(grpcObject.getKey());
        receiveRequest.setTo(grpcObject.getTo());
        receiveRequest.setRaw(Boolean.valueOf(grpcObject.getIsRaw()));

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
