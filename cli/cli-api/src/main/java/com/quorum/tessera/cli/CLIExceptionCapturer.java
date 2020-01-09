package com.quorum.tessera.cli;

import picocli.CommandLine;

import java.io.PrintWriter;

// This method usually returns an exit code for a given exception whilst parsing and handling commandline args
// however, we have a different system in place to do this.
// So here a dummy exit code is returned with access to the exception thrown
public class CLIExceptionCapturer
        implements CommandLine.IExecutionExceptionHandler, CommandLine.IParameterExceptionHandler {

    private Exception thrown;

    @Override
    public int handleExecutionException(
            final Exception ex, final CommandLine cmd, final CommandLine.ParseResult result) {
        this.thrown = ex;
        return 0;
    }

    @Override
    public int handleParseException(final CommandLine.ParameterException ex, final String[] args) {
        // This is an exception type from the library, handle as normal
        if (ex.getCause() == null) {
            final CommandLine cmd = ex.getCommandLine();
            final PrintWriter writer = cmd.getErr();

            writer.println(ex.getMessage());
            cmd.usage(writer);
        }

        // this exception was caused by us, throw it later
        this.thrown = (Exception) ex.getCause();
        return 0;
    }

    public Exception getThrown() {
        return thrown;
    }
}
