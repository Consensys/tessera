package com.quorum.tessera.multitenancy.migration;

import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.CliType;
import com.quorum.tessera.cli.parsers.ConfigConverter;
import com.quorum.tessera.config.Config;
import picocli.CommandLine;

public class Main {

  public static void main(String... args) {
    System.setProperty(
        "javax.xml.bind.JAXBContextFactory", "org.eclipse.persistence.jaxb.JAXBContextFactory");
    System.setProperty(
        "javax.xml.bind.context.factory", "org.eclipse.persistence.jaxb.JAXBContextFactory");
    System.setProperty(CliType.CLI_TYPE_KEY, CliType.MULTITENANCY_MIGRATION.name());

    try {
      final CommandLine commandLine = new CommandLine(new MigrationCliAdapter());
      commandLine
          .registerConverter(Config.class, new ConfigConverter())
          .setSeparator(" ")
          .setCaseInsensitiveEnumValuesAllowed(true);

      commandLine.execute(args);
      final CliResult cliResult = commandLine.getExecutionResult();

      System.exit(cliResult.getStatus());
    } catch (final Exception ex) {
      System.err.println(ex.toString());
      System.exit(1);
    }
  }
}
