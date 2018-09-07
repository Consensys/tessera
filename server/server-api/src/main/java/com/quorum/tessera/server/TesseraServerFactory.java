package com.quorum.tessera.server;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.ServerConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public interface TesseraServerFactory {

    TesseraServer createServer(ServerConfig serverConfig,Object... args);

    static TesseraServerFactory create(CommunicationType communicationType) {
        List<TesseraServerFactory> all = new ArrayList<>();
        ServiceLoader.load(TesseraServerFactory.class).forEach(all::add);
        return all.stream()
                .filter(f -> f.communicationType() == communicationType)
                .findFirst().get();
    }

    CommunicationType communicationType();
    
}
