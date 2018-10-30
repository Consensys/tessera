package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.cli.parsers.ConfigurationParser;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

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
            formatter.setWidth(200);
            formatter.printHelp("tessera admin", options);
            return new CliResult(0, true, null);
        }

        final CommandLine line = new DefaultParser().parse(options, args);
        if(!line.hasOption("addpeer")) {
            System.out.println("No peer defined");
            return new CliResult(1, true, null);
        }

        Config config = new ConfigurationParser().parse(line);

        //TODO revisit - maybe the admin stuff should be reached via unix socket - in order to avoid security concerns
        ServerConfig serverConfig = config.getP2PServerConfig();

        Client restClient = clientFactory.buildFrom(serverConfig);

        String peerUrl = line.getOptionValue("addpeer");
    
        final Peer peer = new Peer(peerUrl);

        String scheme = Optional.of(serverConfig)
                .map(ServerConfig::getBindingUri)
                .map(URI::getScheme)
                .orElse("http");
        
        Integer port = Optional.of(serverConfig)
                .map(ServerConfig::getServerSocket)
                .map(ss -> ((InetServerSocket)ss).getPort())
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

        if(response.getStatus() == Response.Status.CREATED.getStatusCode()) {

            System.out.printf("Peer %s added.",response.getLocation());
            System.out.println();
            
            return new CliResult(0, true, null);
        }
        
        System.err.println("Unable to create peer");
        
        return new CliResult(1, true, null);
    }

}
