
package com.quorum.tessera.reflect;

import org.junit.Test;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;


public class ReflectCallbackTest {
    
    @Test(expected = ReflectException.class)
    public void execute() throws Exception {
        ReflectCallback callback = mock(ReflectCallback.class);
        
        doThrow(ClassNotFoundException.class)
                .when(callback)
                .doExecute();
        
        ReflectCallback.execute(callback);
        
        
    }
    
}
