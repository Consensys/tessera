package com.quorum.tessera.config.apps;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TesseraAppFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(TesseraAppFactory.class);

    private final List<TesseraApp> cache = new ArrayList<>();

    private static final TesseraAppFactory INSTANCE = new TesseraAppFactory();

    public static Set<TesseraApp> create(CommunicationType communicationType, AppType appType) {
        return INSTANCE.createApp(communicationType, appType);
    }

    private TesseraAppFactory() {
        ServiceLoader<TesseraApp> serviceLoader = ServiceLoader.load(TesseraApp.class);
        Iterator<TesseraApp> it = serviceLoader.iterator();

        it.forEachRemaining(cache::add);
        cache.forEach(
                (app) -> {
                    LOGGER.info("Loaded app {}", app);
                });

        LOGGER.info("Cached {}", cache);
    }

    private Set<TesseraApp> createApp(CommunicationType communicationType, AppType appType) {

        return cache.stream()
                .filter(a -> a.getAppType() == appType)
                .filter(a -> a.getCommunicationType() == communicationType)
                .collect(Collectors.toSet());
    }
}
