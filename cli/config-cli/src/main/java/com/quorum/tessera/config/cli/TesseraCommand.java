package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.Config;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@CommandLine.Command(
        name = "tessera",
        headerHeading = "Usage:%n%n",
        header = "Tessera private transaction manager for Quorum",
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        description = "Start a Tessera node.  Other commands exist to manage Tessera encryption keys",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n",
        abbreviateSynopsis = true)
public class TesseraCommand {

    @CommandLine.Option(
            names = {"--configfile", "-configfile"},
            description = "Path to node configuration file")
    public Config config;

    @CommandLine.Option(
            names = {"--pidfile", "-pidfile"},
            description = "the path to write the PID to")
    public Path pidFilePath;

    @CommandLine.Option(
            names = {"-o", "--override"},
            paramLabel = "KEY=VALUE")
    private Map<String, String> overrides = new LinkedHashMap<>();

    @CommandLine.Mixin public DebugOptions debugOptions;

    @CommandLine.Unmatched public List<String> unmatchedEntries;

    // TODO(cjh) dry run option to print effective config to terminal to allow review of CLI overrides
}
