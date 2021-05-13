package com.quorum.tessera.cli;

import java.io.PrintWriter;
import picocli.CommandLine;

// Implementations of these methods are usually used to determine a command's exit codes for
// different exceptions
// encountered during parsing and handling of commandline args.  Tessera, however, has a different
// system for exit code handling.
// So instead we capture any exception for later inspection and return a dummy exit code.
public class CLIExceptionCapturer
    implements CommandLine.IExecutionExceptionHandler, CommandLine.IParameterExceptionHandler {

  private Exception thrown;

  // handleExecutionException is called whenever an exception is encountered during the execution of
  // a picocli
  // Command's business logic.
  // Captures exception and, if the exception is a CliException (indicating some invalid user
  // input), prints cmd usage help.
  @Override
  public int handleExecutionException(
      final Exception ex, final CommandLine cmd, final CommandLine.ParseResult result) {
    if (ex instanceof CliException) {
      final PrintWriter writer = cmd.getErr();
      cmd.usage(writer);
    }

    this.thrown = ex;
    return 0;
  }

  // handleParseException is called whenever invalid user input is detected by picocli framework.
  // Prints cmd usage help and captures exception msg wrapped in CliException for correct exit code
  // handling in tessera's higher layers
  @Override
  public int handleParseException(final CommandLine.ParameterException ex, final String[] args) {
    final CommandLine cmd = ex.getCommandLine();
    final PrintWriter writer = cmd.getErr();
    cmd.usage(writer);

    this.thrown = new CliException(ex.getMessage());

    return 0;
  }

  public Exception getThrown() {
    return thrown;
  }
}
