package com.quorum.tessera.admin.cli.subcommands;

import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.parsers.ConfigurationMixin;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.io.SystemAdapter;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import picocli.CommandLine;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;

import static java.util.Objects.isNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@CommandLine.Command(
        name = "addpeer",
        aliases = {"-addpeer"},
        headerHeading = "Usage:%n%n",
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n",
        header = "Add a new peer to the local node",
        description =
                "Calls the 'PUT /config/peers' API endpoint over REST to add a new peer to the local node, with which it will start exchanging network information")
public class AddPeerCommand implements Callable<CliResult> {

    @CommandLine.Option(names = "help", usageHelp = true, description = "display this help message")
    private boolean isHelpRequested;

    @CommandLine.Mixin private ConfigurationMixin configMixin;

    private final SystemAdapter sys;

    private final ClientFactory clientFactory;

    @CommandLine.Parameters(index = "0", description = "the URL of the peer to add")
    private String peerUrl = null;

    public AddPeerCommand() {
        this(new ClientFactory(), SystemAdapter.INSTANCE);
    }

    public AddPeerCommand(final ClientFactory clientFactory, final SystemAdapter systemAdapter) {
        this.clientFactory = Objects.requireNonNull(clientFactory);
        this.sys = Objects.requireNonNull(systemAdapter);
    }

    @Override
    public CliResult call() {
        if (isNull(configMixin) || isNull(configMixin.getConfig()) || isNull(peerUrl)) {
            return new CliResult(1, true, null);
        }

        final List<ServerConfig> serverConfigs = configMixin.getConfig().getServerConfigs();

        // TODO revisit - maybe the admin stuff should be reached via unix socket - in order to avoid security concerns
        ServerConfig serverConfig =
                serverConfigs.stream()
                        .filter(c -> c.getApp() == AppType.ADMIN)
                        .findFirst()
                        .orElse(serverConfigs.stream().findAny().get());

        Client restClient = clientFactory.buildFrom(serverConfig);

        final Peer peer = new Peer(peerUrl);

        String scheme = Optional.of(serverConfig).map(ServerConfig::getBindingUri).map(URI::getScheme).orElse("http");

        Integer port = Optional.of(serverConfig).map(ServerConfig::getServerUri).map(URI::getPort).orElse(80);

        URI uri = UriBuilder.fromUri(serverConfig.getBindingUri()).port(port).scheme(scheme).build();

        Response response =
                restClient
                        .target(uri)
                        .path("config")
                        .path("peers")
                        .request(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .put(Entity.entity(peer, APPLICATION_JSON));

        if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
            sys.out().printf("Peer %s added.", response.getLocation());
            sys.out().println();

            return new CliResult(0, true, null);
        }

        sys.err().println("Unable to create peer");
        return new CliResult(1, true, null);
    }

    // setters for testing
    public void setPeerUrl(final String peerUrl) {
        this.peerUrl = peerUrl;
    }

    public void setConfigMixin(final ConfigurationMixin configMixin) {
        this.configMixin = configMixin;
    }
}
