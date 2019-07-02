package com.quorum.tessera.test.grpc;

import com.google.protobuf.ByteString;
import com.quorum.tessera.grpc.api.*;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
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

    private final PartyHelper partyHelper = PartyHelper.create();

    private final Party partyOne = partyHelper.findByAlias("A");

    private final Party partyTWo = partyHelper.findByAlias("B");

    @Before
    public void onSetUp() {
        channel1 =
                ManagedChannelBuilder.forAddress(partyOne.getQ2TUri().getHost(), partyOne.getQ2TUri().getPort())
                        .usePlaintext()
                        .build();
        channel2 =
                ManagedChannelBuilder.forAddress(partyTWo.getQ2TUri().getHost(), partyOne.getQ2TUri().getPort())
                        .usePlaintext()
                        .build();

        blockingStub1 = APITransactionGrpc.newBlockingStub(channel1);
        blockingStub2 = APITransactionGrpc.newBlockingStub(channel2);
    }

    @After
    public void onTearDown() {
        channel1.shutdownNow();
        channel2.shutdownNow();
    }

    @Test
    public void sendToSingleRecipient() {

        ByteString payload = ByteString.copyFromUtf8("Zm9v");

        SendRequest request =
                SendRequest.newBuilder()
                        .setFrom(partyOne.getPublicKey())
                        .addTo(partyTWo.getPublicKey())
                        .setPayload(payload)
                        .build();

        SendResponse result = blockingStub1.send(request);

        assertThat(result).isNotNull();
        assertThat(result.getKey()).isNotNull().isNotBlank();

        // confirm that the message has been propagated to the target tessera (config2.json)
        String hash = result.getKey();

        ReceiveRequest receiveRequest = ReceiveRequest.newBuilder().setKey(hash).setTo(partyTWo.getPublicKey()).build();

        ReceiveResponse receiveResponse = blockingStub2.receive(receiveRequest);

        assertThat(receiveResponse).isNotNull();
        assertThat(receiveResponse.getPayload()).isNotNull().isEqualTo(payload);
    }

    @Test
    public void sendSingleTransactionToMultipleParties() {
        SendRequest request =
                SendRequest.newBuilder()
                        .setFrom(partyOne.getPublicKey())
                        .addTo(partyTWo.getPublicKey())
                        .addTo(partyHelper.findByAlias("C").getPublicKey())
                        .setPayload(ByteString.copyFromUtf8("Zm9v"))
                        .build();

        SendResponse result = blockingStub1.send(request);

        assertThat(result).isNotNull();
        assertThat(result.getKey()).isNotNull().isNotBlank();
    }

    @Test
    public void sendTransactionWithNoSender() {
        SendRequest request =
                SendRequest.newBuilder()
                        .addTo(partyTWo.getPublicKey())
                        .setPayload(ByteString.copyFromUtf8("Zm9v"))
                        .build();

        SendResponse result = blockingStub1.send(request);

        assertThat(result).isNotNull();
        assertThat(result.getKey()).isNotNull().isNotBlank();
    }

    @Test
    public void missingPayloadFails() {

        SendRequest request =
                SendRequest.newBuilder().setFrom(partyOne.getPublicKey()).addTo(partyTWo.getPublicKey()).build();
        try {
            SendResponse result = blockingStub1.send(request);
            failBecauseExceptionWasNotThrown(StatusRuntimeException.class);
        } catch (StatusRuntimeException ex) {
            assertThat(ex.getStatus()).isEqualTo(Status.INVALID_ARGUMENT);
        }
    }
}
