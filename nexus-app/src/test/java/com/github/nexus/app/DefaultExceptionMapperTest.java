
package com.github.nexus.app;

import javax.ws.rs.core.Response;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

public class DefaultExceptionMapperTest {
    
    private DefaultExceptionMapper instance;
    
    public DefaultExceptionMapperTest() {
    }
    

    
    @Before
    public void setUp() {
        instance = new DefaultExceptionMapper();
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void toResponse() {
        
        final String message = "OUCH That's gotta smart!!";
        
        Exception exception = new Exception(message);
        
        Response result =  instance.toResponse(exception);
        
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(500);
        assertThat(result.getEntity()).isEqualTo(message);
        
    }
}
