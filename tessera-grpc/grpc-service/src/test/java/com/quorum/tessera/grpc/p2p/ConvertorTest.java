package com.quorum.tessera.grpc.p2p;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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

        DeleteRequest grpcRequest = DeleteRequest.newBuilder()
                .setKey(key)
                .build();

        com.quorum.tessera.api.model.DeleteRequest result = Convertor.toModel(grpcRequest);

        assertThat(result).isNotNull();
        assertThat(result.getKey()).isEqualTo(key);
    }

    @Test
    public void toModelResendRequest() {

        ResendRequest resendRequest = ResendRequest.newBuilder()
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

        ResendRequest resendRequest = ResendRequest.newBuilder()
                .setKey("KEY")
                .setPublicKey("PUBLICKEY")
                .build();

        com.quorum.tessera.partyinfo.ResendRequest result = Convertor.toModel(resendRequest);

        assertThat(result.getKey()).isEqualTo("KEY");
        assertThat(result.getPublicKey()).isEqualTo("PUBLICKEY");
        assertThat(result.getType()).isEqualTo(com.quorum.tessera.partyinfo.ResendRequestType.ALL);

    }

    @Test
    public void toModelResendRequestTypeDefinedType() {

        ResendRequest resendRequest = ResendRequest.newBuilder()
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

}
