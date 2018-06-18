
package com.github.nexus.socket;

import com.github.nexus.configuration.Configuration;
import java.util.concurrent.Executors;

public interface SocketServerFactory {
    
    static SocketServer createSocketServer(Configuration config) {
        return new SocketServer(config,new HttpProxyFactory(),config.uri(),
                Executors.newSingleThreadExecutor());
    }
    
}
