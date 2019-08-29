package com.quorum.tessera.cli;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.Config;

import java.util.Arrays;
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

    public CliResult execute(String... args) throws Exception {

        final boolean adminArgPresent = Arrays.asList(args).contains("admin");

        final CliAdapter cliAdapter =
                ServiceLoaderUtil.loadAll(CliAdapter.class)
                        .filter(p -> adminArgPresent == (p.getType() == CliType.ADMIN))
                        .findFirst()
                        .orElseThrow(
                                () -> new CliException("No valid implementation of CliAdapter found on the classpath"));

        final CliResult result = cliAdapter.execute(args);

        this.config = result.getConfig().orElse(null);
        return result;
    }
}
