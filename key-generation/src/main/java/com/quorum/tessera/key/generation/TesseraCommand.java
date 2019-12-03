package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.Config;
import picocli.CommandLine;

import java.nio.file.Path;

@CommandLine.Command(
    headerHeading = "Usage:%n%n",
    header = "Tessera private transaction manager for Quorum",
    synopsisHeading = "%n",
    descriptionHeading = "%nDescription:%n%n",
    description = "Start a Tessera node.  Other commands exist to manage Tessera encryption keys",
    parameterListHeading = "%nParameters:%n",
    optionListHeading = "%nOptions:%n",
    abbreviateSynopsis = true
)
public class TesseraCommand {

    @CommandLine.Option(
        names = {"--configfile", "-configfile"},
        description = "Path to node configuration file"
    )
    public Config config;

    @CommandLine.Option(
        names = {"--pidfile", "-pidfile"},
        description = "the path to write the PID to"
    )
    public Path pidFilePath;

}
