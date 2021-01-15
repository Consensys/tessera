package com.quorum.tessera.config.cli;

import picocli.CommandLine;

@CommandLine.Command(
    name = "version",
    headerHeading = "Usage:%n%n",
    header = "Print version information and exit",
    commandListHeading = "%nCommands:%n",
    subcommands = {CommandLine.HelpCommand.class},
    versionProvider = VersionProvider.class)
public class VersionCommand {

}
