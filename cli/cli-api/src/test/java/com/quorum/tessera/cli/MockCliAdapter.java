package com.quorum.tessera.cli;

import java.util.concurrent.Callable;
import picocli.CommandLine;

// Static methods allow the type of the mock and the result of execute() to be set statically in
// tests.
// The mock will then be retrieved by the ServiceLoader in CliDelegate.
@CommandLine.Command
public class MockCliAdapter implements CliAdapter, Callable<CliResult> {

  @CommandLine.Option(names = "help", usageHelp = true)
  private boolean isHelpRequested;

  private static CliType t;

  private static CliResult r;

  @picocli.CommandLine.Unmatched private String[] allParameters = new String[0];

  public static void setType(CliType type) {
    t = type;
  }

  public static void setResult(CliResult result) {
    r = result;
  }

  public static void reset() {
    t = null;
    r = null;
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
    return r;
  }
}
