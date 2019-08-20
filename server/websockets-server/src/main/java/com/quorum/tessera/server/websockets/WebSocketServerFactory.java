package com.quorum.tessera.server.websockets;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.apps.TesseraAppClass;
import com.quorum.tessera.server.TesseraServer;
import com.quorum.tessera.server.TesseraServerFactory;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WebSocketServerFactory implements TesseraServerFactory<TesseraAppClass> {

    @Override
    public TesseraServer createServer(ServerConfig config, Set<TesseraAppClass> services) {
        return new WebSocketServer(
                config,
                services.stream().map(TesseraAppClass::getAppClass).flatMap(Stream::of).collect(Collectors.toSet()));
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.WEB_SOCKET;
    }
}
