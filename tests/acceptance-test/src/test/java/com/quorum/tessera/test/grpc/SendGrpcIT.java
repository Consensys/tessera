package com.quorum.tessera.test.grpc;

import com.quorum.tessera.api.grpc.TransactionGrpc;
import com.quorum.tessera.api.grpc.model.SendRequest;
import com.quorum.tessera.api.grpc.model.SendResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class SendGrpcIT {

    private ManagedChannel channel;

    private TransactionGrpc.TransactionBlockingStub blockingStub;

    @Before
    public void onSetUp() {
        channel = ManagedChannelBuilder.forAddress("127.0.0.1", 8080)
                .usePlaintext()
                .build();

        blockingStub = TransactionGrpc.newBlockingStub(channel);
    }

    @After
    public void onTearDown() {
        channel.shutdown();

    }

    @Test
    public void sendToSingleRecipient() throws Exception {

        SendRequest request = SendRequest.newBuilder()
                .setFrom("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=")
                .addTo("yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0=")
                .setPayload("Zm9v")
                .build();

        SendResponse result = blockingStub.send(request);

        assertThat(result).isNotNull();
        result.getAllFields().forEach((k, v) -> System.out.println(k + " " + v));
        assertThat(result.getKey()).isNotNull().isNotBlank();

    }

    @Test
    public void sendSingleTransactionToMultipleParties() {
        SendRequest request = SendRequest.newBuilder()
                .setFrom("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=")
                .addTo("yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0=")
                .addTo("giizjhZQM6peq52O7icVFxdTmTYinQSUsvyhXzgZqkE=")
                .setPayload("Zm9v")
                .build();

        SendResponse result = blockingStub.send(request);

        assertThat(result).isNotNull();
        result.getAllFields().forEach((k, v) -> System.out.println(k + " " + v));
        assertThat(result.getKey()).isNotNull().isNotBlank();

    }

//    @Test
//    public void missingPayloadFails() throws Exception {
//        
//        SendRequest request = SendRequest.newBuilder()
//                .setFrom("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=")
//                .addTo("yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0=")
//                .build();
//        try {
//        SendResponse result = blockingStub.send(request);
//        } catch(StatusRuntimeException ex) {
//            ex.getStatus().
//        }
//        assertThat(result).isNotNull();
//        result.getAllFields().forEach((k, v) -> System.out.println(k + " " + v));
//        assertThat(result.getKey()).isNotNull().isNotBlank();
//        
//    }

    /*
        @Test
    public void missingPayloadFails() {

        final String sendRequest = Json.createObjectBuilder()
            .add("from", "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=")
            .add("to", Json.createArrayBuilder().add("yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0="))
            .build().toString();

        LOGGER.info("sendRequest: {}", sendRequest);

        final Response response = client.target(SERVER_URI)
            .path(SEND_PATH)
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

        //validate result

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(400);
    }
     */
}
