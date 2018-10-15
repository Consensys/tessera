package com.quorum.tessera.api.grpc;

import com.google.protobuf.ByteString;
import com.quorum.tessera.api.grpc.model.*;
import com.quorum.tessera.transaction.TransactionManager;

import io.grpc.stub.StreamObserver;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;

public class TransactionGrpcService extends TransactionGrpc.TransactionImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionGrpcService.class);

    private final Validator validator = Validation.byDefaultProvider()
            .configure().ignoreXmlConfiguration().buildValidatorFactory().getValidator();

    private final TransactionManager transactionManager;

    public TransactionGrpcService(TransactionManager transactionManager) {
        this.transactionManager = Objects.requireNonNull(transactionManager);
    }

    @Override
    public void send(SendRequest grpcSendRequest, StreamObserver<SendResponse> responseObserver) {

        StreamObserverTemplate template = new StreamObserverTemplate(responseObserver);

        template.handle(() -> {

            com.quorum.tessera.api.model.SendRequest sendRequest = Convertor.toModel(grpcSendRequest);

            Set<ConstraintViolation<com.quorum.tessera.api.model.SendRequest>> violations = validator.validate(sendRequest);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
            com.quorum.tessera.api.model.SendResponse response = transactionManager.send(sendRequest);

            return Convertor.toGrpc(response);
        });

    }

    @Override
    public void receive(ReceiveRequest grpcRequest, StreamObserver<ReceiveResponse> responseObserver) {

        StreamObserverTemplate template = new StreamObserverTemplate(responseObserver);

        template.handle(() -> {

            com.quorum.tessera.api.model.ReceiveRequest request = Convertor.toModel(grpcRequest);
            Set<ConstraintViolation<com.quorum.tessera.api.model.ReceiveRequest>> violations = validator.validate(request);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }

            com.quorum.tessera.api.model.ReceiveRequest receiveRequest = Convertor.toModel(grpcRequest);

            com.quorum.tessera.api.model.ReceiveResponse receiveResponse = transactionManager.receive(receiveRequest);

            ByteString payload = ByteString.copyFrom(receiveResponse.getPayload());
            return ReceiveResponse
                    .newBuilder()
                    .setPayload(payload)
                    .build();
        });

    }

    @Override
    public void delete(DeleteRequest grpcRequest, StreamObserver<DeleteRequest> responseObserver) {
        LOGGER.debug("Received delete key request");

        StreamObserverTemplate template = new StreamObserverTemplate(responseObserver);

        template.handle(() -> {

            com.quorum.tessera.api.model.DeleteRequest request = Convertor.toModel(grpcRequest);

            Set<ConstraintViolation<com.quorum.tessera.api.model.DeleteRequest>> violations = validator.validate(request);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }

            transactionManager.delete(request);
            return grpcRequest;
        });

    }

    @Override
    public void resend(ResendRequest grpcRequest, StreamObserver<ResendResponse> responseObserver) {
        LOGGER.debug("Received resend request");

        StreamObserverTemplate template = new StreamObserverTemplate(responseObserver);

        template.handle(() -> {
            com.quorum.tessera.api.model.ResendRequest request = Convertor.toModel(grpcRequest);

            Optional<byte[]> result = transactionManager.resend(request).getPayload();

            ResendResponse.Builder builder = ResendResponse.newBuilder();
            result.map(ByteString::copyFrom).ifPresent(builder::setData);
            return builder.build();
        });

    }

    @Override
    public void push(PushRequest request, StreamObserver<PushRequest> responseObserver) {
        LOGGER.debug("Received push request");

        StreamObserverTemplate template = new StreamObserverTemplate(responseObserver);

        template.handle(() -> {
            transactionManager.storePayload(request.toByteArray());
            return request;
        });

    }

}
