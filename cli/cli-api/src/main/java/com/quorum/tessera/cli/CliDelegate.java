package com.quorum.tessera.cli;

import com.quorum.tessera.config.Config;

import java.util.Optional;

public enum CliDelegate {
    INSTANCE;

    private Config config;

    public static CliDelegate instance() {
        return INSTANCE;
    }

    public Config getConfig() {
        return Optional.ofNullable(config)
                .orElseThrow(
                        () -> new IllegalStateException("Execute must be invoked before attempting to fetch config"));
    }

    public void setConfig(Config config) {
        this.config = config;
    }
}
