package com.quorum.tessera.config.cli.admin.subcommands;

import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.parsers.ConfigurationMixin;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.config.cli.DebugOptions;
import com.quorum.tessera.config.util.ConfigFileStore;
import picocli.CommandLine;

import java.util.concurrent.Callable;

import static java.util.Objects.isNull;

@CommandLine.Command(
    name = "addpeer",
    aliases = {"-addpeer"},
    headerHeading = "Usage:%n%n",
    synopsisHeading = "%n",
    descriptionHeading = "%nDescription:%n%n",
    parameterListHeading = "%nParameters:%n",
    optionListHeading = "%nOptions:%n",
    header = "Add a new peer to the local node",
    subcommands = {CommandLine.HelpCommand.class},
    description =
        "Calls the 'PUT /config/peers' API endpoint over REST to add a new peer to the local node, with which it will start exchanging network information")
public class AddPeerCommand implements Callable<CliResult> {

    @CommandLine.Mixin
    private ConfigurationMixin configMixin;

    @CommandLine.Parameters(index = "0", description = "the URL of the peer to add")
    private String peerUrl = null;

    @CommandLine.Mixin public DebugOptions debugOptions;

    @Override
    public CliResult call() {
        if (isNull(configMixin) || isNull(configMixin.getConfig()) || isNull(peerUrl)) {
            return new CliResult(1, true, null);
        }

        final Config config = configMixin.getConfig();
        config.addPeer(new Peer(peerUrl));
        ConfigFileStore.get().save(config);

        System.out.println("Added peer to config file. Please restart node to apply update");

        return new CliResult(0, true, config);

    }

    // setters for testing
    public void setPeerUrl(final String peerUrl) {
        this.peerUrl = peerUrl;
    }

    public void setConfigMixin(final ConfigurationMixin configMixin) {
        this.configMixin = configMixin;
    }
}
