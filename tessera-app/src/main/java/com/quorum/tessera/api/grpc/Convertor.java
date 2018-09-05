package com.quorum.tessera.api.grpc;

import com.quorum.tessera.api.grpc.model.SendResponse;
import com.quorum.tessera.api.model.DeleteRequest;
import com.quorum.tessera.api.model.ReceiveRequest;
import com.quorum.tessera.api.model.ResendRequest;
import com.quorum.tessera.api.model.ResendRequestType;
import java.util.Arrays;
import java.util.stream.Stream;

public class Convertor {
    
    private Convertor() {
        throw new UnsupportedOperationException("This object should not be constructed.");
    }
    
    static DeleteRequest toModel(com.quorum.tessera.api.grpc.model.DeleteRequest grpcRequest) {
        DeleteRequest request = new DeleteRequest(); 
        request.setKey(grpcRequest.getKey());
        
        return request;
    }


    static com.quorum.tessera.api.model.SendRequest toModel(com.quorum.tessera.api.grpc.model.SendRequest grpcObject) {
        com.quorum.tessera.api.model.SendRequest sendRequest =  new com.quorum.tessera.api.model.SendRequest();
        sendRequest.setTo(grpcObject.getToList().toArray(new String[0]));
        sendRequest.setFrom(grpcObject.getFrom());
        sendRequest.setPayload(grpcObject.getPayload());
        return sendRequest;
    }

    static com.quorum.tessera.api.grpc.model.SendRequest toGrpc(com.quorum.tessera.api.model.SendRequest modelObject) {
        return com.quorum.tessera.api.grpc.model.SendRequest.newBuilder()
                .setFrom(modelObject.getFrom())
                .setPayload(modelObject.getPayload())
                .addAllTo(Arrays.asList(modelObject.getTo()))
                .build();
    }


    static ReceiveRequest toModel(com.quorum.tessera.api.grpc.model.ReceiveRequest grpcObject) {
        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(grpcObject.getKey());
        receiveRequest.setTo(grpcObject.getTo());

        return receiveRequest;
    }

    static com.quorum.tessera.api.grpc.model.ReceiveRequest toGrpc(ReceiveRequest modelObject) {
        return com.quorum.tessera.api.grpc.model.ReceiveRequest.newBuilder()
                .setKey(modelObject.getKey())
                .setTo(modelObject.getTo())
                .build();
    }

    static com.quorum.tessera.api.model.ResendRequest toModel(com.quorum.tessera.api.grpc.model.ResendRequest grpcObject) {
        com.quorum.tessera.api.model.ResendRequest resendRequest = new ResendRequest();
        resendRequest.setKey(grpcObject.getKey());
        resendRequest.setPublicKey(grpcObject.getPublicKey());
       
        Stream.of(com.quorum.tessera.api.grpc.model.ResendRequestType.values())
                .map(Enum::name)
                .filter(n -> n.equals(grpcObject.getType().name()))
                .map(ResendRequestType::valueOf)
                .findAny().ifPresent(resendRequest::setType);
      
        return resendRequest;
    }
    
    static SendResponse toGrpc(com.quorum.tessera.api.model.SendResponse response) {
        return SendResponse.newBuilder()
                .setKey(response.getKey())
                .build();
    }
 
}
