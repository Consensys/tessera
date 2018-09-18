package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.Config;

import java.util.Objects;
import java.util.Optional;

public class CliResult {
    
    private final Integer status;
    private final boolean isHelpOn;
    private final boolean isKeyGenOn;
    private final Config config;

    public CliResult(Integer status, boolean isHelpOn, boolean isKeyGenOn, Config config) {
        this.status = Objects.requireNonNull(status);
        this.isHelpOn = isHelpOn;
        this.isKeyGenOn = isKeyGenOn;
        this.config = config;
    }

    public Integer getStatus() {
        return status;
    }

    public boolean isHelpOn() {
        return isHelpOn;
    }

    public boolean isKeyGenOn() {return isKeyGenOn;}

    public Optional<Config> getConfig() {
        return Optional.ofNullable(config);
    }
}
