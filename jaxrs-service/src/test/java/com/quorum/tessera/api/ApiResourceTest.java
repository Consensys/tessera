package com.quorum.tessera.api;

import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApiResourceTest {

    private ApiResource apiResource;

    public ApiResourceTest() {

    }

    @Before
    public void setUp() {
        apiResource = new ApiResource();
    }

    @After
    public void tearDown() {
        apiResource = null;
    }

    @Test
    public void testGetSchema()  throws Exception  {
        Request request = mock(Request.class);
        Variant variant = mock(Variant.class);
        
        when(variant.getMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
        
        when(request.selectVariant(any(List.class))).thenReturn(variant);
        
       Response result =  apiResource.api(request);
        
       assertThat(result.getEntity()).isNotNull();
       assertThat(result.getStatus()).isEqualTo(200);
       assertThat(result.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);
        
    }
    
    @Test
    public void testGetHtmlDoc()  throws Exception  {
        Request request = mock(Request.class);
        Variant variant = mock(Variant.class);
        
        when(variant.getMediaType()).thenReturn(MediaType.TEXT_HTML_TYPE);
        
        when(request.selectVariant(any(List.class))).thenReturn(variant);
        
       Response result =  apiResource.api(request);
        
       assertThat(result.getEntity()).isNotNull();
       assertThat(result.getStatus()).isEqualTo(200);
       assertThat(result.getMediaType()).isEqualTo(MediaType.TEXT_HTML_TYPE);
        
    }
    
    @Test
    public void unsupported()  throws Exception  {
        Request request = mock(Request.class);
        Variant variant = mock(Variant.class);
        
        when(variant.getMediaType()).thenReturn(MediaType.APPLICATION_OCTET_STREAM_TYPE);
        
        when(request.selectVariant(any(List.class))).thenReturn(variant);
        
       Response result =  apiResource.api(request);
        
       assertThat(result.getStatus()).isEqualTo(400);
        
    }
}
