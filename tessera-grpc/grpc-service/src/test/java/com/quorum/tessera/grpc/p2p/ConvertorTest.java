package com.quorum.tessera.grpc.p2p;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

import com.google.protobuf.ByteString;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

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
    public void toModelDeleteRequest() {

        String key = "Some Key";

        DeleteRequest grpcRequest = DeleteRequest.newBuilder().setKey(key).build();

        com.quorum.tessera.api.model.DeleteRequest result = Convertor.toModel(grpcRequest);

        assertThat(result).isNotNull();
        assertThat(result.getKey()).isEqualTo(key);
    }

    @Test
    public void toModelResendRequest() {

        ResendRequest resendRequest =
                ResendRequest.newBuilder()
                        .setKey("KEY")
                        .setPublicKey("PUBLICKEY")
                        .setType(ResendRequestType.ALL)
                        .build();

        com.quorum.tessera.partyinfo.ResendRequest result = Convertor.toModel(resendRequest);

        assertThat(result.getKey()).isEqualTo("KEY");
        assertThat(result.getPublicKey()).isEqualTo("PUBLICKEY");
        assertThat(result.getType()).isEqualTo(com.quorum.tessera.partyinfo.ResendRequestType.ALL);
    }

    @Test
    public void toModelResendRequestNoTypeDefinedType() {

        ResendRequest resendRequest = ResendRequest.newBuilder().setKey("KEY").setPublicKey("PUBLICKEY").build();

        com.quorum.tessera.partyinfo.ResendRequest result = Convertor.toModel(resendRequest);

        assertThat(result.getKey()).isEqualTo("KEY");
        assertThat(result.getPublicKey()).isEqualTo("PUBLICKEY");
        assertThat(result.getType()).isEqualTo(com.quorum.tessera.partyinfo.ResendRequestType.ALL);
    }

    @Test
    public void toModelResendRequestTypeDefinedType() {

        ResendRequest resendRequest =
                ResendRequest.newBuilder()
                        .setKey("KEY")
                        .setType(ResendRequestType.INDIVIDUAL)
                        .setPublicKey("PUBLICKEY")
                        .build();

        com.quorum.tessera.partyinfo.ResendRequest result = Convertor.toModel(resendRequest);

        assertThat(result.getKey()).isEqualTo("KEY");
        assertThat(result.getPublicKey()).isEqualTo("PUBLICKEY");
        assertThat(result.getType()).isEqualTo(com.quorum.tessera.partyinfo.ResendRequestType.INDIVIDUAL);
    }

    @Test
    public void toGrpcResendRequestAll() throws Exception {

        com.quorum.tessera.partyinfo.ResendRequest request = new com.quorum.tessera.partyinfo.ResendRequest();
        request.setKey("KEY");
        request.setPublicKey("PUBLIC_KEY");
        request.setType(com.quorum.tessera.partyinfo.ResendRequestType.ALL);

        ResendRequest result = Convertor.toGrpc(request);
        assertThat(result).isNotNull();
        assertThat(result.getKey()).isEqualTo("KEY");
        assertThat(result.getPublicKey()).isEqualTo("PUBLIC_KEY");
        assertThat(result.getType()).isEqualTo(ResendRequestType.ALL);
    }

    @Test
    public void toGrpcResendRequestNoType() throws Exception {

        com.quorum.tessera.partyinfo.ResendRequest request = new com.quorum.tessera.partyinfo.ResendRequest();
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

        com.quorum.tessera.partyinfo.ResendRequest request = new com.quorum.tessera.partyinfo.ResendRequest();
        request.setKey("KEY");
        request.setPublicKey("PUBLIC_KEY");
        request.setType(com.quorum.tessera.partyinfo.ResendRequestType.INDIVIDUAL);

        ResendRequest result = Convertor.toGrpc(request);
        assertThat(result).isNotNull();
        assertThat(result.getKey()).isEqualTo("KEY");
        assertThat(result.getPublicKey()).isEqualTo("PUBLIC_KEY");
        assertThat(result.getType()).isEqualTo(ResendRequestType.INDIVIDUAL);
    }

    @Test
    public void toGrpcResendBatchRequest() throws Exception {

        com.quorum.tessera.partyinfo.ResendBatchRequest request = new com.quorum.tessera.partyinfo.ResendBatchRequest();
        request.setPublicKey("PUBLIC_KEY");
        request.setBatchSize(100);

        ResendBatchRequest result = Convertor.toGrpc(request);
        assertThat(result).isNotNull();
        assertThat(result.getPublicKey()).isEqualTo("PUBLIC_KEY");
        assertThat(result.getBatchSize()).isEqualTo(100);
    }

    @Test
    public void toModelResendBatchRequest() {

        ResendBatchRequest resendRequest =
                ResendBatchRequest.newBuilder().setBatchSize(100).setPublicKey("PUBLICKEY").build();

        com.quorum.tessera.partyinfo.ResendBatchRequest result = Convertor.toModel(resendRequest);

        assertThat(result.getBatchSize()).isEqualTo(100);
        assertThat(result.getPublicKey()).isEqualTo("PUBLICKEY");
    }

    @Test
    public void toGrpcResendBatchResponse() throws Exception {

        com.quorum.tessera.partyinfo.ResendBatchResponse request =
                new com.quorum.tessera.partyinfo.ResendBatchResponse();
        request.setTotal(100);

        ResendBatchResponse result = Convertor.toGrpc(request);
        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isEqualTo(100);
    }

    @Test
    public void toModelResendBatchResponse() {

        ResendBatchResponse resendRequest = ResendBatchResponse.newBuilder().setTotal(100).build();

        com.quorum.tessera.partyinfo.ResendBatchResponse result = Convertor.toModel(resendRequest);

        assertThat(result.getTotal()).isEqualTo(100);
    }

    @Test
    public void toGrpcPushBatchRequest() throws Exception {

        com.quorum.tessera.partyinfo.PushBatchRequest request = new com.quorum.tessera.partyinfo.PushBatchRequest();
        request.setEncodedPayloads(Collections.singletonList("data".getBytes()));

        PushBatchRequest result = Convertor.toGrpc(request);
        assertThat(result).isNotNull();
        assertThat(result.getDataList()).containsExactly(ByteString.copyFromUtf8("data"));
    }

    @Test
    public void toModelPushBatchRequest() {

        PushBatchRequest resendRequest = PushBatchRequest.newBuilder().addData(ByteString.copyFromUtf8("DATA")).build();

        com.quorum.tessera.partyinfo.PushBatchRequest result = Convertor.toModel(resendRequest);

        assertThat(result.getEncodedPayloads().size()).isEqualTo(1);
    }
}
