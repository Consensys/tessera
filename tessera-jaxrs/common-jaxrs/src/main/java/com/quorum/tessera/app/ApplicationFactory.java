package com.quorum.tessera.app;

import com.quorum.tessera.config.AppType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationFactory.class);

    private final List<TesseraRestApplication> cache = new ArrayList<>();

    private static final ApplicationFactory INSTANCE = new ApplicationFactory();

    public static Optional<TesseraRestApplication> create(AppType appType) {
        return INSTANCE.createApp(appType);
    }

    private ApplicationFactory() {
        Iterator<TesseraRestApplication> it = ServiceLoader.load(TesseraRestApplication.class).iterator();
        while (it.hasNext()) {
            TesseraRestApplication app = it.next();
            LOGGER.info("Loaded app {}", app);
            cache.add(app);
        }
    }

    private Optional<TesseraRestApplication> createApp(AppType appType) {
        LOGGER.info("Creating application for {}", appType);
        return cache.stream().filter(a -> a.getAppType() == appType)
                .findAny();
    }

}
