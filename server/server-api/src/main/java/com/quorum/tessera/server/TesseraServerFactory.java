package com.quorum.tessera.server;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.ServerConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

public interface TesseraServerFactory<T> {

    TesseraServer createServer(ServerConfig config, Set<T> services);

    static TesseraServerFactory create(CommunicationType communicationType) {
        CommunicationType ct = communicationType == CommunicationType.UNIX_SOCKET ? CommunicationType.REST : communicationType;
        List<TesseraServerFactory> all = new ArrayList<>();
        ServiceLoader.load(TesseraServerFactory.class).forEach(all::add);
        return all.stream()
                .filter(f -> f.communicationType() == ct)
                .findFirst().get();
    }

    CommunicationType communicationType();
    
}
