package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.Config;
import picocli.CommandLine;

import java.nio.file.Path;

public class KeyGenFileUpdateOptions {
    @CommandLine.Option(
            names = {"--configfile", "-configfile"},
            description = "Path to node configuration file")
    public Config config;

    // TODO(cjh) implement config output and password file update ?
    //  we've removed the ability to start the node straight away after generating keys.  Not sure if updating
    //  configfile
    //  and password file is something we want to still support or put onus on users to go and update as required
    @CommandLine.Option(
            names = {"--configout", "-output"},
            description = "Path to save updated configfile to.  Requires --configfile option to also be provided")
    public Path configOut;

    @CommandLine.Option(
            names = {"--pwdout"},
            description =
                    "Path to save updated password list to.  Requires --configfile and --configout options to also be provided")
    public Path pwdOut;

    public Config getConfig() {
        return config;
    }

    public Path getConfigOut() {
        return configOut;
    }

    public Path getPwdOut() {
        return pwdOut;
    }
}
