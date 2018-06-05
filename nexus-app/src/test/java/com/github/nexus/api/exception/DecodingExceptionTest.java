
package com.github.nexus.api.exception;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class DecodingExceptionTest {

    public DecodingExceptionTest() {
    }

     @Test
    public void constructWithMessage() {
    
        String message = "Some punk's busted up my ride!!";
        
        DecodingException decodingException = new DecodingException(message);
        
        assertThat(decodingException.getMessage()).isEqualTo(message);

        
    }
    
         @Test
    public void constructWithMessageAndCause() {
    
        String message = "Some punk's busted up my ride!!";
        Throwable cause = new Exception("OUCH");
        DecodingException decodingException = new DecodingException(message,cause);
        
        assertThat(decodingException.getMessage()).isEqualTo(message);
        assertThat(decodingException.getCause()).isSameAs(cause);
    }
    
    
    @Test
    public void constructWithCause() {
    
        Throwable cause = new Exception("OUCH");
        DecodingException decodingException = new DecodingException(cause);
        
        assertThat(decodingException.getMessage()).isEqualTo("java.lang.Exception: OUCH");
        assertThat(decodingException.getCause()).isSameAs(cause);
        
    }
}
