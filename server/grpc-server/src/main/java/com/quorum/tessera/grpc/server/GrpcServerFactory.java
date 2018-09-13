package com.quorum.tessera.grpc.server;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.server.TesseraServer;
import com.quorum.tessera.server.TesseraServerFactory;
import io.grpc.BindableService;
import io.grpc.ServerBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class GrpcServerFactory implements TesseraServerFactory {


    @Override
    public TesseraServer createServer(ServerConfig serverConfig, Set<Object> services) {

        if (Objects.nonNull(serverConfig.getGrpcPort())) {
            final List<BindableService> bindableServices = services
                .stream()
                .filter(BindableService.class::isInstance)
                .map(o -> (BindableService) o)
                .collect(Collectors.toList());

            final URI serverUri = serverConfig.getGrpcUri();

            ServerBuilder serverBuilder = ServerBuilder.forPort(serverUri.getPort());

            bindableServices.stream().forEach(serverBuilder::addService);

            return new GrpcServer(serverUri, serverBuilder.build());
        }

        return null;

    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.GRPC;
    }
}
