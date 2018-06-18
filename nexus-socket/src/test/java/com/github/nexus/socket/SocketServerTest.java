
package com.github.nexus.socket;

import com.github.nexus.configuration.Configuration;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class SocketServerTest {
    
    private SocketServer socketServer;
           
    private Configuration config;
    
    private HttpProxyFactory httpProxyFactory;
    
    private URI uri;
    
    private ExecutorService executorService;
    
    
    public SocketServerTest() {
    }
    
    @Before
    public void setUp() throws URISyntaxException {
        config = mock(Configuration.class);
        when(config.workdir())
                .thenReturn(System.getProperty("java.io.tmpdir"));
        when(config.socket()).thenReturn("socket_file");
        
        httpProxyFactory = mock(HttpProxyFactory.class);
        uri = new URI("http://bogos.com:9819");
        executorService = mock(ExecutorService.class);
        
        socketServer = new SocketServer(config, httpProxyFactory, uri,executorService);
    }
    
    @After
    public void tearDown() {
        verifyNoMoreInteractions(httpProxyFactory,executorService);  
    }
    
    
    @Test
    public void start() {
        socketServer.start();
        verify(executorService).submit(socketServer);
        
    }
    
    @Test
    public void stop() {
        socketServer.stop();
        verify(executorService).shutdown();
        
    }
    
    
   // @Test
    public void run() {
        
        HttpProxy httpProxy = mock(HttpProxy.class);
        when(httpProxy.connect()).thenReturn(true);
        
        when(httpProxyFactory.create(uri)).thenReturn(httpProxy);
        
        socketServer.run();
        
        
        
    }
    
}
