package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.Config;

public enum CliDelegate {

    INSTANCE;

    private Config config;

    public static CliDelegate instance() {
        return INSTANCE;
    }

    public Config getConfig() {
        return config;
    }

    public CliResult execute(String... args) throws Exception {

        final CliAdapter cliAdapter = CliAdapter.create();

        final CliResult result = cliAdapter.execute(args);

        this.config = result.getConfig().orElse(null);
        return result;
    }

}
