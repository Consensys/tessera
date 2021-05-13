package com.quorum.tessera.config.cli;

import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.config.Version;
import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(
    name = "version",
    header = "Print version information and exit",
    commandListHeading = "%nCommands:%n",
    abbreviateSynopsis = true,
    subcommands = {CommandLine.HelpCommand.class})
public class VersionCommand implements Callable<CliResult> {

  @Override
  public CliResult call() {
    System.out.println(Version.getVersion());
    return new CliResult(0, true, null);
  }
}
