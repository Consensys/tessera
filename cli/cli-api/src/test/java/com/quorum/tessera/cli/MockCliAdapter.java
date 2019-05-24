package com.quorum.tessera.cli;

// Static methods allow the type of the mock and the result of execute() to be set statically in tests.
// The mock will then be retrieved by the ServiceLoader in CliDelegate.
public class MockCliAdapter implements CliAdapter {
    private static CliType t;

    private static CliResult r;

    public static void setType(CliType type) {
        t = type;
    }

    public static void setResult(CliResult result) {
        r = result;
    }

    @Override
    public CliType getType() {
        return t;
    }

    @Override
    public CliResult execute(String... args) throws Exception {
        return r;
    }
}
