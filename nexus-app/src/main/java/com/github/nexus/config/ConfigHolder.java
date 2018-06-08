package com.github.nexus.config;

import java.util.Objects;

public enum ConfigHolder {

    INSTANCE;

    private Configuration config;

    private boolean initialised;

    ConfigHolder() {
        this.initialised = false;
    }

    public synchronized void setConfiguration(final Configuration config) {
        if(initialised) {
            throw new RuntimeException("Configuration already set");
        }

        this.initialised = true;
        this.config = Objects.requireNonNull(config);
    }

    public static ConfigHolder getInstance() {
        return INSTANCE;
    }

    public Configuration getConfig() {
        return config;
    }

}
