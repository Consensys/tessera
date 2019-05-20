package com.quorum.tessera.cli;

public class MockDefaultCliAdapter implements CliAdapter {
    @Override
    public CliType getType() {
        return CliType.CONFIG;
    }

    @Override
    public CliResult execute(String... args) throws Exception {
        return new CliResult(100, false, null);
    }
}
