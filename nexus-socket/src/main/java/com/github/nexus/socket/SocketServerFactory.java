
package com.github.nexus.socket;

import com.github.nexus.configuration.Configuration;
import com.github.nexus.junixsocket.adapter.UnixSocketFactory;

import java.util.concurrent.Executors;

public interface SocketServerFactory {

    static SocketServer createSocketServer(final Configuration config) {
        return new SocketServer(
            config,
            new HttpProxyFactory(config.uri()),
            Executors.newCachedThreadPool(),
            UnixSocketFactory.create()
        );
    }

}
