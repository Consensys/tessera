
package com.quorum.tessera.api.exception;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.core.Response;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class EntityNotFoundExceptionMapperTest {
    
    private EntityNotFoundExceptionMapper instance;
    
    public EntityNotFoundExceptionMapperTest() {
    }

    @Before
    public void setUp() {
        instance = new EntityNotFoundExceptionMapper();
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void toResponse() {
        
        final String message = "OUCH That's gotta smart!!";
        
        EntityNotFoundException exception = new EntityNotFoundException(message);
        
        Response result =  instance.toResponse(exception);
        
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(404);
        assertThat(result.getEntity()).isEqualTo(message);
        
    }
    
}
