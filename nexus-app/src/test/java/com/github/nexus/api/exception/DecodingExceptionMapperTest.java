
package com.github.nexus.api.exception;

import javax.ws.rs.core.Response;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DecodingExceptionMapperTest {
    
    private DecodingExceptionMapper instance;
    
    public DecodingExceptionMapperTest() {
    }
    

    
    @Before
    public void setUp() {
        instance = new DecodingExceptionMapper();
    }
    
    @After
    public void tearDown() {
    }


    @Test
     public void toResponse() {
     
         DecodingException decodingException = new DecodingException("OUCH");
         
         Response result = instance.toResponse(decodingException);
         assertThat(result).isNotNull();
         
         String message = (String) result.getEntity();
         
         assertThat(message).isEqualTo("OUCH");
         
         assertThat(result.getStatus()).isEqualTo(400);

     }
}
