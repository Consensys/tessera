
package com.github.nexus.config.cli;

import com.github.nexus.config.Config;
import java.util.Objects;
import java.util.Optional;


public class CliResult {
    
    private final Integer status;
    
    private final Config config;

    public CliResult(Integer status, Config config) {
        this.status = Objects.requireNonNull(status);
        this.config = config;
    }

    public Integer getStatus() {
        return status;
    }

    public Optional<Config> getConfig() {
        return Optional.ofNullable(config);
    }
  
}
