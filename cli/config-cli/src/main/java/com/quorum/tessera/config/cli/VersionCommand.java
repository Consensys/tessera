package com.quorum.tessera.config.cli;

import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.config.Version;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
    name = "version",
    headerHeading = "Usage:%n%n",
    header = "Print version information and exit",
    commandListHeading = "%nCommands:%n",
    subcommands = {CommandLine.HelpCommand.class})
public class VersionCommand implements Callable<CliResult> {

    // The versionProvider param of @Command is an alternative way of doing this but we aren't here as we don't want --version and -V options enabled
    @Override
    public CliResult call() {
        System.out.println(Version.getVersion());
        return new CliResult(0, true, null);
    }
}
