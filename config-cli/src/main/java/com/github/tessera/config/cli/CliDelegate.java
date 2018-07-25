package com.github.tessera.config.cli;

import com.github.tessera.config.Config;


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
        
        CliAdapter cliAdapter = CliAdapter.create();

        CliResult result =  cliAdapter.execute(args);
        this.config = result.getConfig().orElse(null);
        return result;
    }


}
