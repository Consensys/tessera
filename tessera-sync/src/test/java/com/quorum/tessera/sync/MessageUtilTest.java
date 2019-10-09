
package com.quorum.tessera.sync;

import org.junit.Test;


public class MessageUtilTest {
    
    
    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotBeConstructed() {
        new MessageUtil();
    }
}
