package com.github.nexus.api.exception;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;

public class NotFoundExceptionMapperTest {
    
    private NotFoundExceptionMapper mapper;
    
    public NotFoundExceptionMapperTest() {
    }
    
    @Before
    public void setUp() {
        mapper = new NotFoundExceptionMapper();
    }
    
    
    @Test
    public void toResponse() {
        
        String message = "What are you talking aboit Willis?!?";
        NotFoundException exception = new NotFoundException(message);
        Response response = mapper.toResponse(exception);
        
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity()).isEqualTo(message);
        
    }
    
    
}
