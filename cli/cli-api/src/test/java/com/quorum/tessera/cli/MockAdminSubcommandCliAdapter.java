package com.quorum.tessera.cli;

import picocli.CommandLine;

import java.util.concurrent.Callable;

/**
 * This is a command that is intended to not be a <main class> command, and gets attached as a subcommand to other CLI
 * adapters.  Its setup mimics that of the AdminCliAdapter subcommand and should be used in cases where testing CLI args containing "admin" are handled correctly.
 */
@CommandLine.Command(name = "admin")
public class MockAdminSubcommandCliAdapter implements CliAdapter, Callable<CliResult> {

    private static final CliType t = CliType.ADMIN;

    private static CliResult r;

    private static Exception exceptionToBeThrown;

    @CommandLine.Option(names = "help", usageHelp = true)
    private boolean isHelpRequested;

    @CommandLine.Unmatched private String[] allParameters = new String[0];

    public static void setResult(CliResult result) {
        r = result;
    }

    public static void setExceptionToBeThrown(final Exception exceptionToBeThrown) {
        MockAdminSubcommandCliAdapter.exceptionToBeThrown = exceptionToBeThrown;
    }

    public static void reset() {
        r = null;
        exceptionToBeThrown = null;
    }

    @Override
    public CliType getType() {
        return t;
    }

    @Override
    public CliResult call() throws Exception {
        return this.execute(allParameters);
    }

    @Override
    public CliResult execute(String... args) throws Exception {
        if (exceptionToBeThrown != null) {
            throw exceptionToBeThrown;
        }
        return r;
    }
}
