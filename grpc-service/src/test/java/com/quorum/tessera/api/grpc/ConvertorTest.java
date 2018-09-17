package com.quorum.tessera.api.grpc;

import com.quorum.tessera.api.grpc.model.ReceiveRequest;
import com.quorum.tessera.api.grpc.model.DeleteRequest;
import com.quorum.tessera.api.grpc.model.ResendRequest;
import com.quorum.tessera.api.grpc.model.ResendRequestType;
import com.quorum.tessera.api.grpc.model.SendRequest;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

public class ConvertorTest {

    @Test
    public void constructUnSupported() throws Exception {
        Constructor c = Convertor.class.getDeclaredConstructor();
        c.setAccessible(true);
        try {
            c.newInstance();
            failBecauseExceptionWasNotThrown(InvocationTargetException.class);
        } catch (InvocationTargetException invocationTargetException) {
            assertThat(invocationTargetException).hasCauseExactlyInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Test
    public void toGrpcReceiveRequest() {

        String key = "Some Key";
        String to = "Mr Benn";

        com.quorum.tessera.api.model.ReceiveRequest receiveRequest
                = new com.quorum.tessera.api.model.ReceiveRequest();

        receiveRequest.setKey(key);
        receiveRequest.setTo(to);

        ReceiveRequest result = Convertor.toGrpc(receiveRequest);

        assertThat(result).isNotNull();

        assertThat(result.getKey()).isEqualTo(key);
        assertThat(result.getTo()).isEqualTo(to);

    }

    @Test
    public void toModelReceiveRequest() {

        String key = "Some Key";
        String to = "Mr Benn";

        ReceiveRequest receiveRequest = ReceiveRequest.newBuilder()
                .setKey(key).setTo(to)
                .build();

        com.quorum.tessera.api.model.ReceiveRequest result = Convertor.toModel(receiveRequest);

        assertThat(result).isNotNull();

        assertThat(result.getKey()).isEqualTo(key);
        assertThat(result.getTo()).isEqualTo(to);

    }

    @Test
    public void toModelDeleteRequest() {

        String key = "Some Key";

        DeleteRequest grpcRequest = DeleteRequest.newBuilder()
                .setKey(key)
                .build();

        com.quorum.tessera.api.model.DeleteRequest result = Convertor.toModel(grpcRequest);

        assertThat(result).isNotNull();
        assertThat(result.getKey()).isEqualTo(key);
    }

    @Test
    public void toGrpcSendRequest() {

        com.quorum.tessera.api.model.SendRequest sendRequest = new com.quorum.tessera.api.model.SendRequest();
        sendRequest.setFrom("FROM");
        sendRequest.setPayload("PAYLOAD");
        sendRequest.setTo(new String[]{"TO1"});

        SendRequest result = Convertor.toGrpc(sendRequest);
        assertThat(result.getFrom()).isEqualTo("FROM");
        assertThat(result.getPayload()).isEqualTo("PAYLOAD");
        assertThat(result.getToList()).containsExactly("TO1");

    }

    @Test
    public void toModelResendRequest() {

        ResendRequest resendRequest = ResendRequest.newBuilder()
                .setKey("KEY")
                .setPublicKey("PUBLICKEY")
                .setType(ResendRequestType.ALL)
                .build();

        com.quorum.tessera.api.model.ResendRequest result = Convertor.toModel(resendRequest);

        assertThat(result.getKey()).isEqualTo("KEY");
        assertThat(result.getPublicKey()).isEqualTo("PUBLICKEY");
        assertThat(result.getType()).isEqualTo(com.quorum.tessera.api.model.ResendRequestType.ALL);

    }

    @Test
    public void toModelResendRequestNoTypeDefinedType() {

        ResendRequest resendRequest = ResendRequest.newBuilder()
                .setKey("KEY")
                .setPublicKey("PUBLICKEY")
                .build();

        com.quorum.tessera.api.model.ResendRequest result = Convertor.toModel(resendRequest);

        assertThat(result.getKey()).isEqualTo("KEY");
        assertThat(result.getPublicKey()).isEqualTo("PUBLICKEY");
        assertThat(result.getType()).isEqualTo(com.quorum.tessera.api.model.ResendRequestType.ALL);

    }

    @Test
    public void toModelResendRequestTypeDefinedType() {

        ResendRequest resendRequest = ResendRequest.newBuilder()
                .setKey("KEY")
                .setType(ResendRequestType.INDIVIDUAL)
                .setPublicKey("PUBLICKEY")
                .build();

        com.quorum.tessera.api.model.ResendRequest result = Convertor.toModel(resendRequest);

        assertThat(result.getKey()).isEqualTo("KEY");
        assertThat(result.getPublicKey()).isEqualTo("PUBLICKEY");
        assertThat(result.getType()).isEqualTo(com.quorum.tessera.api.model.ResendRequestType.INDIVIDUAL);

    }

    @Test
    public void toModelSendRequest() {
        SendRequest grpcSendRequest = SendRequest.newBuilder()
                .setFrom("FROM")
                .setPayload("PAYLOAD")
                .addTo("TO1")
                .addTo("TO2")
                .build();

        com.quorum.tessera.api.model.SendRequest result = Convertor.toModel(grpcSendRequest);
        assertThat(result).isNotNull();
        assertThat(result.getTo()).containsExactly("TO1", "TO2");
        assertThat(result.getFrom()).isEqualTo("FROM");
        assertThat(result.getPayload()).isEqualTo("PAYLOAD");

    }

    @Test
    public void toGrpcResendRequestAll() throws Exception {

        com.quorum.tessera.api.model.ResendRequest request = new com.quorum.tessera.api.model.ResendRequest();
        request.setKey("KEY");
        request.setPublicKey("PUBLIC_KEY");
        request.setType(com.quorum.tessera.api.model.ResendRequestType.ALL);

        ResendRequest result = Convertor.toGrpc(request);
        assertThat(result).isNotNull();
        assertThat(result.getKey()).isEqualTo("KEY");
        assertThat(result.getPublicKey()).isEqualTo("PUBLIC_KEY");
        assertThat(result.getType()).isEqualTo(ResendRequestType.ALL);

    }

    @Test
    public void toGrpcResendRequestNoType() throws Exception {

        com.quorum.tessera.api.model.ResendRequest request = new com.quorum.tessera.api.model.ResendRequest();
        request.setKey("KEY");
        request.setPublicKey("PUBLIC_KEY");

        ResendRequest result = Convertor.toGrpc(request);
        assertThat(result).isNotNull();
        assertThat(result.getKey()).isEqualTo("KEY");
        assertThat(result.getPublicKey()).isEqualTo("PUBLIC_KEY");
        assertThat(result.getType()).isEqualTo(ResendRequestType.INDIVIDUAL);

    }

    @Test
    public void toGrpcResendRequestIndividualType() throws Exception {

        com.quorum.tessera.api.model.ResendRequest request = new com.quorum.tessera.api.model.ResendRequest();
        request.setKey("KEY");
        request.setPublicKey("PUBLIC_KEY");
        request.setType(com.quorum.tessera.api.model.ResendRequestType.INDIVIDUAL);

        ResendRequest result = Convertor.toGrpc(request);
        assertThat(result).isNotNull();
        assertThat(result.getKey()).isEqualTo("KEY");
        assertThat(result.getPublicKey()).isEqualTo("PUBLIC_KEY");
        assertThat(result.getType()).isEqualTo(ResendRequestType.INDIVIDUAL);

    }

}
