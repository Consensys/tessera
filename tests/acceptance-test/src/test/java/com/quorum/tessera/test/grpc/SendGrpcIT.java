package com.quorum.tessera.test.grpc;

import com.google.protobuf.ByteString;
import com.quorum.tessera.grpc.api.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SendGrpcIT {

    private ManagedChannel channel1;
    private ManagedChannel channel2;

    private APITransactionGrpc.APITransactionBlockingStub blockingStub1;
    private APITransactionGrpc.APITransactionBlockingStub blockingStub2;

    @Before
    public void onSetUp() {
        channel1 = ManagedChannelBuilder.forAddress("127.0.0.1", 50520)
                .usePlaintext()
                .build();
        channel2 = ManagedChannelBuilder.forAddress("127.0.0.1", 50521)
                .usePlaintext()
                .build();

        blockingStub1 = APITransactionGrpc.newBlockingStub(channel1);
        blockingStub2 = APITransactionGrpc.newBlockingStub(channel2);
    }

    @After
    public void onTearDown() {
        channel1.shutdown();

    }

    @Test
    public void sendToSingleRecipient() {

        ByteString payload = ByteString.copyFromUtf8("Zm9v");

        SendRequest request = SendRequest.newBuilder()
                .setFrom("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=")
                .addTo("yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0=")
                .setPayload(payload)
                .build();

        SendResponse result = blockingStub1.send(request);

        assertThat(result).isNotNull();
        result.getAllFields().forEach((k, v) -> System.out.println(k + " " + v));
        assertThat(result.getKey()).isNotNull().isNotBlank();

        // confirm that the message has been propagated to the target tessera (config2.json)
        String hash = result.getKey();

        ReceiveRequest receiveRequest = ReceiveRequest.newBuilder()
            .setKey(hash)
            .setTo("yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0=")
            .build();

        ReceiveResponse receiveResponse = blockingStub2.receive(receiveRequest);

        assertThat(receiveResponse).isNotNull();
        receiveResponse.getAllFields().forEach((k, v) -> System.out.println(k + " " + v));
        assertThat(receiveResponse.getPayload()).isNotNull().isEqualTo(payload);
    }

    @Test
    public void sendSingleTransactionToMultipleParties() {
        SendRequest request = SendRequest.newBuilder()
                .setFrom("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=")
                .addTo("yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0=")
                .addTo("giizjhZQM6peq52O7icVFxdTmTYinQSUsvyhXzgZqkE=")
                .setPayload(ByteString.copyFromUtf8("Zm9v"))
                .build();

        SendResponse result = blockingStub1.send(request);

        assertThat(result).isNotNull();
        result.getAllFields().forEach((k, v) -> System.out.println(k + " " + v));
        assertThat(result.getKey()).isNotNull().isNotBlank();

    }

    @Test
    public void missingPayloadFails() {

        SendRequest request = SendRequest.newBuilder()
                .setFrom("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=")
                .addTo("yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0=")
                .build();
        try {
            SendResponse result = blockingStub1.send(request);
            failBecauseExceptionWasNotThrown(StatusRuntimeException.class);
        } catch (StatusRuntimeException ex) {
            assertThat(ex.getStatus()).isEqualTo(Status.INVALID_ARGUMENT);
        }

    }
}
