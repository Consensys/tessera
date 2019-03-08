package com.quorum.tessera.grpc.api;

import com.google.protobuf.ByteString;
import com.quorum.tessera.grpc.StreamObserverTemplate;
import com.quorum.tessera.transaction.TransactionManager;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Objects;
import java.util.Set;

public class APITransactionGrpcService extends APITransactionGrpc.APITransactionImplBase{

    private static final Logger LOGGER = LoggerFactory.getLogger(APITransactionGrpcService.class);

    private final Validator validator = Validation.byDefaultProvider()
            .configure().ignoreXmlConfiguration().buildValidatorFactory().getValidator();

    private final TransactionManager transactionManager;

    public APITransactionGrpcService(TransactionManager transactionManager) {
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
    
    
}
