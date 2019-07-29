package com.quorum.tessera.config.apps;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TesseraAppFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(TesseraAppFactory.class);

    private final List<TesseraApp> cache = new ArrayList<>();

    private static final TesseraAppFactory INSTANCE = new TesseraAppFactory();

    public static Optional<TesseraApp> create(CommunicationType communicationType,AppType appType) {
        return INSTANCE.createApp(communicationType,appType);
    }

    private TesseraAppFactory() {
        Iterator<TesseraApp> it = ServiceLoader.load(TesseraApp.class).iterator();
        while (it.hasNext()) {
            TesseraApp app = it.next();
            LOGGER.info("Loaded app {}", app);
            cache.add(app);
        }
    }

    private Optional<TesseraApp> createApp(CommunicationType communicationType,AppType appType) {
        LOGGER.info("Creating application type {} for {}", appType,communicationType);
        return cache.stream()
                .filter(a -> a.getAppType() == appType)
                .filter(a -> a.getCommunicationType()== communicationType)
                .findAny();
    }
}
