package com.quorum.tessera.api.grpc;

import com.google.protobuf.ByteString;
import com.quorum.tessera.api.grpc.model.*;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.model.MessageHash;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.transaction.PayloadEncoderImpl;
import com.quorum.tessera.transaction.model.EncodedPayloadWithRecipients;
import com.quorum.tessera.util.Base64Decoder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public class TransactionGrpcService extends TransactionGrpc.TransactionImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionGrpcService.class);

    private final Enclave enclave;

    private final Base64Decoder base64Decoder;

    public TransactionGrpcService(Enclave enclave, Base64Decoder base64Decoder) {
        this.enclave = requireNonNull(enclave);
        this.base64Decoder = requireNonNull(base64Decoder);
    }

    @Override
    public void send(SendRequest sendRequest, StreamObserver<SendResponse> responseObserver) {

        LOGGER.debug("Received send request from gRPC");

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

    @Override
    public void delete(DeleteRequest request, StreamObserver<DeleteRequest> responseObserver) {
        LOGGER.debug("Received delete key request");

        final byte[] hashBytes = base64Decoder.decode(request.getKey());
        enclave.delete(hashBytes);
        responseObserver.onNext(request);
        responseObserver.onCompleted();
    }

    @Override
    public void resend(ResendRequest request, StreamObserver<ResendResponse> responseObserver) {
        LOGGER.debug("Received resend request");

        final byte[] publicKey = base64Decoder.decode(request.getPublicKey());

        if (request.getType() == ResendRequestType.ALL) {
            enclave.resendAll(publicKey);
            responseObserver.onNext(ResendResponse.newBuilder().build());
        } else {
            final byte[] hashKey = base64Decoder.decode(request.getKey());

            final EncodedPayloadWithRecipients payloadWithRecipients = enclave
                .fetchTransactionForRecipient(new MessageHash(hashKey), new Key(publicKey));

            final byte[] encoded = new PayloadEncoderImpl().encode(payloadWithRecipients);
            responseObserver.onNext(ResendResponse.newBuilder()
                .setData(ByteString.copyFrom(encoded))
                .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void push(PushRequest request, StreamObserver<PushRequest> responseObserver) {
        LOGGER.debug("Received push request");

        final MessageHash messageHash = enclave.storePayload(request.toByteArray());

        LOGGER.info(base64Decoder.encodeToString(messageHash.getHashBytes()));

        responseObserver.onNext(request);
        responseObserver.onCompleted();
    }
}
