package com.quorum.tessera.admin.cli;

import com.quorum.tessera.cli.CliAdapter;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.parsers.ConfigurationParser;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import org.apache.commons.cli.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Cli Adapter to be used for runtime updates
 */
public class AdminCliAdapter implements CliAdapter {

    private final ClientFactory clientFactory;

    public AdminCliAdapter(final ClientFactory clientFactory) {
        this.clientFactory = Objects.requireNonNull(clientFactory);
    }

    /**
     *
     * @param args
     * @return CliResult with config object always null.
     * @throws Exception
     */
    @Override
    public CliResult execute(String... args) throws Exception {

        final Options options = new Options();

        options.addOption(
                Option.builder("configfile")
                        .desc("Path to node configuration file")
                        .hasArg(true)
                        .required()
                        .optionalArg(false)
                        .numberOfArgs(1)
                        .argName("PATH")
                        .build());

        options.addOption(Option.builder("addpeer")
                .desc("Add peer to running node")
                .hasArg(true)
                .optionalArg(false)
                .numberOfArgs(1)
                .argName("URL")
                .build());

        final List<String> argsList = Arrays.asList(args);

        if (argsList.contains("help") || argsList.isEmpty()) {
            HelpFormatter formatter = new HelpFormatter();
            PrintWriter pw = new PrintWriter(sys().out());
            formatter.printHelp(pw,
                    200, "tessera admin",
                    null, options, formatter.getLeftPadding(),
                    formatter.getDescPadding(), null, false);
            pw.flush();

            return new CliResult(0, true, null);
        }

        final CommandLine line = new DefaultParser().parse(options, args);
        if (!line.hasOption("addpeer")) {
            sys().out().println("No peer defined");
            return new CliResult(1, true, null);
        }

        Config config = new ConfigurationParser().parse(line);

        //TODO revisit - maybe the admin stuff should be reached via unix socket - in order to avoid security concerns
        ServerConfig serverConfig = config.getServerConfigs().stream()
                .filter(c -> c.getApp() == AppType.ADMIN)
                .findFirst().orElse(config.getServerConfigs().stream().findAny().get());

        Client restClient = clientFactory.buildFrom(serverConfig);

        String peerUrl = line.getOptionValue("addpeer");

        final Peer peer = new Peer(peerUrl);

        String scheme = Optional.of(serverConfig)
                .map(ServerConfig::getBindingUri)
                .map(URI::getScheme)
                .orElse("http");

        Integer port = Optional.of(serverConfig)
                .map(ServerConfig::getServerUri)
                .map(URI::getPort)
                .orElse(80);

        URI uri = UriBuilder.fromUri(serverConfig.getBindingUri())
                .port(port)
                .scheme(scheme)
                .build();

        Response response = restClient.target(uri)
                .path("config")
                .path("peers")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .put(Entity.entity(peer, MediaType.APPLICATION_JSON));

        if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {

            sys().out().printf("Peer %s added.", response.getLocation());
            sys().out().println();

            return new CliResult(0, true, null);
        }

        sys().err().println("Unable to create peer");

        return new CliResult(1, true, null);
    }

}
