package com.quorum.tessera.data.migration;

import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.CliType;
import com.quorum.tessera.cli.parsers.ConfigConverter;
import com.quorum.tessera.config.Config;
import java.util.Arrays;
import picocli.CommandLine;

public class Main {

  private Main() {
    throw new UnsupportedOperationException("");
  }

  public static void main(final String... args) {

    System.setProperty(CliType.CLI_TYPE_KEY, CliType.DATA_MIGRATION.name());

    try {
      final CommandLine commandLine = new CommandLine(new CmdLineExecutor());
      commandLine
          .registerConverter(Config.class, new ConfigConverter())
          .setSeparator(" ")
          .setCaseInsensitiveEnumValuesAllowed(true);

      commandLine.execute(args);
      final CliResult cliResult = commandLine.getExecutionResult();

      System.exit(cliResult.getStatus());
    } catch (final Exception ex) {
      System.err.println("An error has occurred: " + ex.getMessage());

      if (Arrays.asList(args).contains("debug")) {
        System.err.println();
        System.err.println("Exception message: " + ex.getMessage());
        System.err.println("Exception class: " + ex.getClass());
      }

      System.exit(1);
    }
  }
}
