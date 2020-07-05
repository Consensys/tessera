package com.quorum.tessera.config.apps;

import com.quorum.tessera.loader.ServiceLoaderUtil;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TesseraAppFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(TesseraAppFactory.class);

    private final List<TesseraApp> cache = new ArrayList<>();

    private static final TesseraAppFactory INSTANCE = new TesseraAppFactory();

    public static Optional<TesseraApp> create(CommunicationType communicationType, AppType appType) {
        LOGGER.info("Create from {} {}", communicationType, appType);
        return INSTANCE.createApp(communicationType, appType);
    }

    private TesseraAppFactory() {
        ServiceLoaderUtil.loadAll(TesseraApp.class).peek(app -> LOGGER.info("Loaded app {}", app)).forEach(cache::add);
    }

    private Optional<TesseraApp> createApp(CommunicationType communicationType, AppType appType) {
        LOGGER.info("Creating application type {} for {}", appType, communicationType);
        return cache.stream()
                .filter(a -> a.getAppType() == appType)
                .filter(a -> a.getCommunicationType() == communicationType)
                .findAny();
    }
}
