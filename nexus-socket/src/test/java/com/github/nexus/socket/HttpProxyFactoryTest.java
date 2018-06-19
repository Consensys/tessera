
package com.github.nexus.socket;

import java.net.URI;
import java.net.URISyntaxException;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;


public class HttpProxyFactoryTest {
    
    private HttpProxyFactory httpProxyFactory;
    
    public HttpProxyFactoryTest() {
    }
    
    @Before
    public void setUp() {
         httpProxyFactory= new HttpProxyFactory();
    }

    @Test
    public void create() throws URISyntaxException {
        URI uri = new URI("http://bogus.com");
        HttpProxy httpProxy =  httpProxyFactory.create(uri);
        assertThat(httpProxy).isNotNull();
    
    }
    
}
