
package com.github.nexus.socket;

import com.github.nexus.configuration.Configuration;

public interface SocketServerFactory {
    
    static SocketServer createSocketServer(Configuration config) {
        return new SocketServer(config,new HttpProxyFactory(),config.uri());
    }
    
}
