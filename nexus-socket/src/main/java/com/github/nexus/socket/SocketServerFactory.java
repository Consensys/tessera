
package com.github.nexus.socket;

import com.github.nexus.configuration.Configuration;
import com.github.nexus.junixsocket.adapter.UnixSocketFactory;

import java.nio.file.Paths;
import java.util.concurrent.Executors;

public interface SocketServerFactory {

    static SocketServer createSocketServer(final Configuration config) throws Exception {
        return new SocketServer(
            Paths.get(config.workdir(), config.socket()),
            new HttpProxyFactory(config),
            Executors.newCachedThreadPool(),
            UnixSocketFactory.create()
        );
    }

}
