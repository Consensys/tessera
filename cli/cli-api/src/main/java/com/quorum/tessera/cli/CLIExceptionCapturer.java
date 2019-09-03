package com.quorum.tessera.cli;

import picocli.CommandLine;

// This method usually returns an exit code for a given exception whilst parsing and handling commandline args
// however, we have a different system in place to do this.
// So here a dummy exit code is returned with access to the exception thrown
public class CLIExceptionCapturer implements CommandLine.IExecutionExceptionHandler {

    private Exception thrown;

    @Override
    public int handleExecutionException(
            final Exception ex, final CommandLine cmd, final CommandLine.ParseResult result) {
        this.thrown = ex;
        return 0;
    }

    public Exception getThrown() {
        return thrown;
    }
}
