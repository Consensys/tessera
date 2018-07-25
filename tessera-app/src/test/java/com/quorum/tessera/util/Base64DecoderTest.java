package com.quorum.tessera.util;

import com.quorum.tessera.util.exception.DecodingException;
import org.junit.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

public class Base64DecoderTest {

    public Base64DecoderTest() {
    }

    @Test(expected = DecodingException.class)
    public void invalidBase64DataThrowsDecodeException() {
        Base64Decoder.create().decode("1");
    }
    
    @Test
    public void decode() {
        
        byte[] result = Base64Decoder.create().decode("ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=");
        
        assertThat(result).isNotEmpty();
    }
    
    @Test
    public void encodeToString() {
        
      byte[] data = "BOGUS".getBytes();  
      
      String expected = Base64.getEncoder().encodeToString(data);
        
      String result = Base64Decoder.create().encodeToString(data);
      assertThat(result).isEqualTo(expected);
    }

}
