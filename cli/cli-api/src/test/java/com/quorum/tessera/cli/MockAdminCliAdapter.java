package com.quorum.tessera.cli;

public class MockAdminCliAdapter implements CliAdapter {
    @Override
    public CliType getType() {
        return CliType.ADMIN;
    }

    @Override
    public CliResult execute(String... args) throws Exception {
        return new CliResult(101, true, null);
    }
}
