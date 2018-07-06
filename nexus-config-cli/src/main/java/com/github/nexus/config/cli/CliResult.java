
package com.github.nexus.config.cli;

import com.github.nexus.config.Config;
import java.util.Objects;
import java.util.Optional;


public class CliResult {
    
    private final Integer status;
    private final boolean isHelpOn;
    private final Config config;

    public CliResult(Integer status, boolean isHelpOn, Config config) {
        this.status = Objects.requireNonNull(status);
        this.isHelpOn = isHelpOn;
        this.config = config;
    }

    public Integer getStatus() {
        return status;
    }

    public boolean isHelpOn() {
        return isHelpOn;
    }

    public Optional<Config> getConfig() {
        return Optional.ofNullable(config);
    }
}
