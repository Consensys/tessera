package com.quorum.tessera.cli;

import com.quorum.tessera.config.Config;

import java.util.Optional;

// TODO(cjh) still using CliDelegate as a config store so that config can be injected by spring
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
