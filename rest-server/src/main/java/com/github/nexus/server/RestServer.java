
package com.github.nexus.server;

import java.util.ServiceLoader;

public interface RestServer {
        
    void start() throws Exception;
    
    void stop() throws Exception;
    
    
    
}
