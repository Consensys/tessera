
package com.quorum.tessera.exception;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class TesseraExceptionTest {

    @Test
    public void createWithString() {
        TesseraException sample = new MyTesseraException("OUCH");
        assertThat(sample).hasMessage("OUCH");
    }
    
    
    static class MyTesseraException extends TesseraException {
        
        MyTesseraException(String message) {
            super(message);
        }
        
    }
    
}
