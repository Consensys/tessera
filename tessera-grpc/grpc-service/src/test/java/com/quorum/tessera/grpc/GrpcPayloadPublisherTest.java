
package com.quorum.tessera.grpc;

import com.quorum.tessera.client.GrpcP2pClient;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import static org.assertj.core.api.Assertions.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class GrpcPayloadPublisherTest {
    
    private GrpcPayloadPublisher publisher;
    
    private GrpcP2pClient p2pClient;
    
    private PayloadEncoder payloadEncoder; 
    
    @Before
    public void onSetUp() {
        payloadEncoder = mock(PayloadEncoder.class);
        p2pClient = mock(GrpcP2pClient.class);
        publisher = new GrpcPayloadPublisher(p2pClient,payloadEncoder);
    }
    
    @After
    public void onTearDown() {
        verifyNoMoreInteractions(p2pClient,payloadEncoder);
    }
    
    @Test
    public void publish() {

        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        byte[] encodedData = "I LOVE SPARROWS".getBytes();
        
        when(payloadEncoder.encode(encodedPayload)).thenReturn(encodedData);
        
        String url = "http://somedomain.com";
        
        publisher.publishPayload(encodedPayload, url);
        
        verify(p2pClient).push(url, encodedData);
        
        verify(payloadEncoder).encode(encodedPayload);
    }
    
    @Test
   public void constructWithDefaultConstructor() {
       assertThat(new GrpcPayloadPublisher()).isNotNull();
   }
    
}
