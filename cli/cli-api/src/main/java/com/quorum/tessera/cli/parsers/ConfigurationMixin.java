package com.quorum.tessera.cli.parsers;

import com.quorum.tessera.config.Config;
import picocli.CommandLine;

public class ConfigurationMixin {

    @CommandLine.Option(names = {"-configfile"}, description = "path to configuration file")
    private Config config;

    public void setConfig(final Config config) {
        this.config = config;
    }

    public Config getConfig() {
        return this.config;
    }
}
