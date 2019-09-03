package com.quorum.tessera.admin.cli;

import com.quorum.tessera.admin.cli.subcommands.AddPeerCommand;
import com.quorum.tessera.cli.CliAdapter;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.CliType;
import picocli.CommandLine;

import java.util.concurrent.Callable;

/** Cli Adapter to be used for runtime updates */
@CommandLine.Command(
        exitCodeOnInvalidInput = 1,
        name = "admin",
        subcommands = {AddPeerCommand.class})
public class AdminCliAdapter implements CliAdapter, Callable<CliResult> {

    @CommandLine.Option(names = "help", usageHelp = true, description = "display this help message")
    private boolean isHelpRequested;

    @Override
    public CliType getType() {
        return CliType.ADMIN;
    }

    /**
     * @param args
     * @return CliResult with config object always null.
     */
    @Override
    public CliResult execute(String... args) {
        return new CliResult(0, true, null);
    }

    @Override
    public CliResult call() {
        return this.execute();
    }
}
