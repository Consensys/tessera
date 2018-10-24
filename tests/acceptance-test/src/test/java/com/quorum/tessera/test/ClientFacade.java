
package com.quorum.tessera.test;

import com.quorum.tessera.test.rest.RestClientFacade;
import java.net.URI;
import java.util.Random;

public interface ClientFacade {
    
    <T> T send(Party sender,byte[] data,Party... recipients);
    
    
    <T> T send(URI senderUri, byte[] transactionData, Party... recipients);
    
    <T> T find(Party party,String hash);
       
    
    boolean isUp(Party party);
    
    enum CommunicationType {
        REST,GRPC;
    }
    
    static ClientFacade create(CommunicationType type) {

        
        if(type == CommunicationType.REST) {
            return new RestClientFacade();
        }
        throw new UnsupportedOperationException();
    }
        
    
    static byte[] generateTransactionData() {
        Random random = new Random();
        byte[] bytes = new byte[random.nextInt(500)];
        random.nextBytes(bytes);
        return bytes;
    }
    
}
