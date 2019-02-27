package com.quorum.tessera.config.cli;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
            
        final CliAdapter cliAdapter;
        
        if(argsList.contains("admin")) {
            cliAdapter = new AdminCliAdapter(new ClientFactory());
        } else {
            cliAdapter = ServiceLoaderUtil.load(CliAdapter.class)
                    .orElse(new DefaultCliAdapter());
        }

        final CliResult result = cliAdapter.execute(args);

        this.config = result.getConfig().orElse(null);
        return result;
    }

}
