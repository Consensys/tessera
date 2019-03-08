package com.quorum.tessera.config.cli;

import com.quorum.tessera.io.SystemAdapter;

public interface CliAdapter {

    CliResult execute(String... args) throws Exception;
    
    default SystemAdapter sys() {
        return SystemAdapter.INSTANCE;
    }
    
}
