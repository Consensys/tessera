package com.quorum.tessera.server;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.ServerConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface TesseraServerFactory<T> {

    TesseraServer createServer(ServerConfig config, Set<T> services);

    Logger LOGGER = LoggerFactory.getLogger(TesseraServerFactory.class);

    static TesseraServerFactory create(CommunicationType communicationType) {

        List<TesseraServerFactory> all = new ArrayList<>();
        ServiceLoader.load(TesseraServerFactory.class).forEach(all::add);

        return all.stream()
                .filter(f -> f.communicationType() == communicationType)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No server factory found for " + communicationType));
    }

    CommunicationType communicationType();
}
