package com.quorum.tessera.api.grpc;

import com.quorum.tessera.api.grpc.model.ReceiveRequest;
import com.quorum.tessera.api.grpc.model.ReceiveResponse;
import com.quorum.tessera.api.grpc.model.SendRequest;
import com.quorum.tessera.api.grpc.model.SendResponse;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.util.Base64Decoder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static java.util.stream.Collectors.joining;

public class GrpcTransactionService extends TransactionGrpc.TransactionImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcTransactionService.class);

    private final Enclave enclave;

    private final Base64Decoder base64Decoder;

    public GrpcTransactionService(Enclave enclave, Base64Decoder base64Decoder) {
        this.enclave = enclave;
        this.base64Decoder = base64Decoder;
    }

    @Override
    public void send(SendRequest sendRequest, StreamObserver<SendResponse> responseObserver) {

        LOGGER.debug("Received send request from Grpc");

        // Null will be handled in the enclave
        final String sender = sendRequest.getFrom().isEmpty() ? null : sendRequest.getFrom();

        final Optional<byte[]> from = Optional.ofNullable(sender)
            .map(base64Decoder::decode);

        LOGGER.debug("SEND: sender {}", sender);

        final byte[][] recipients = sendRequest.getToList()
            .stream()
            .map(base64Decoder::decode)
            .toArray(byte[][]::new);

        LOGGER.debug("SEND: recipients {}", sendRequest.getToList().stream().collect(joining()));

        final byte[] payload = base64Decoder.decode(sendRequest.getPayload());

        final byte[] key = enclave.store(from, recipients, payload).getHashBytes();

        final String encodedKey = base64Decoder.encodeToString(key);
        final SendResponse response = SendResponse.newBuilder().setKey(encodedKey).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void receive(ReceiveRequest request, StreamObserver<ReceiveResponse> responseObserver) {

        final byte[] key = base64Decoder.decode(request.getKey());

        final Optional<byte[]> to = Optional
            .ofNullable(request.getTo())
            .filter(str -> !str.isEmpty())
            .map(base64Decoder::decode);

        final byte[] payload = enclave.receive(key, to);

        final String encodedPayload = base64Decoder.encodeToString(payload);

        final ReceiveResponse response = ReceiveResponse
            .newBuilder()
            .setPayload(encodedPayload)
            .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }
}
