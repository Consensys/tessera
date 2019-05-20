package com.quorum.tessera.cli;

import com.quorum.tessera.config.Config;

import java.util.*;

public enum CliDelegate {

    INSTANCE;

    private Config config;

    public static CliDelegate instance() {
        return INSTANCE;
    }

    public Config getConfig() {
        return Optional.ofNullable(config)
                .orElseThrow(() -> new IllegalStateException("Execute must me invoked before attempting to fetch config"));
    }

    public CliResult execute(String... args) throws Exception {

        final List<String> argsList = Arrays.asList(args);

        List<CliAdapter> providers = new ArrayList<>();
        ServiceLoader.load(CliAdapter.class).forEach(providers::add);

        final CliAdapter cliAdapter = providers.stream()
            .filter(p -> (argsList.contains("admin")) == (p.getType() == CliType.ADMIN))
            .findFirst()
            .orElseThrow(() -> new CliException("No valid implementation of CliAdapter found on the classpath"));

        final CliResult result = cliAdapter.execute(args);

        this.config = result.getConfig().orElse(null);
        return result;
    }

}
