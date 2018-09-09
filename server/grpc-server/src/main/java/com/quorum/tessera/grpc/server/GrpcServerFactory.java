package com.quorum.tessera.grpc.server;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.server.TesseraServer;
import com.quorum.tessera.server.TesseraServerFactory;
import io.grpc.BindableService;
import io.grpc.ServerBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GrpcServerFactory implements TesseraServerFactory {


    @Override
    public TesseraServer createServer(ServerConfig serverConfig, Object... args) {

         Set<Object> servicesBeans = (Set<Object>) args[0];
        
        Integer port = serverConfig.getPort(); 

        
        URI uri = serverConfig.getServerUri();
        
        final List<BindableService> services = servicesBeans
                .stream()
                .map(o -> (BindableService) o)
                .collect(Collectors.toList());

        final URI serverUri;
        try {
            serverUri = new URI(uri.toString() +":"+ port);
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }

        ServerBuilder serverBuilder = ServerBuilder.forPort(port);

        services.stream().forEach(serverBuilder::addService);

        return new GrpcServer(serverUri, serverBuilder.build());

    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.GRPC;
    }
}
