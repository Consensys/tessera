package com.quorum.tessera.grpc.server;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.apps.TesseraApp;
import com.quorum.tessera.grpc.GrpcApp;
import com.quorum.tessera.server.TesseraServer;
import com.quorum.tessera.server.TesseraServerFactory;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Optional;
import java.util.Set;

public class GrpcServerFactory implements TesseraServerFactory<Object> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GrpcServerFactory.class);

  @Override
  public TesseraServer createServer(ServerConfig serverConfig, Set<Object> services) {

    if (serverConfig.getCommunicationType() == CommunicationType.GRPC) {
      final Optional<GrpcApp> grpcApp =
          services.stream()
              .filter(GrpcApp.class::isInstance)
              .filter(TesseraApp.class::isInstance)
              .map(TesseraApp.class::cast)
              .filter(a -> a.getAppType() == serverConfig.getApp())
              .findFirst()
              .map(GrpcApp.class::cast);

      if (grpcApp.isPresent()) {
        final URI serverUri = serverConfig.getServerUri();
        ServerBuilder serverBuilder = ServerBuilder.forPort(serverUri.getPort());
        grpcApp.get().getBindableServices().forEach(serverBuilder::addService);
        return new GrpcServer(serverUri, serverBuilder.build());
      } else {
        LOGGER.info("Unable to find grpc app for " + serverConfig.getApp());
      }
    }

    return null;
  }

  @Override
  public CommunicationType communicationType() {
    return CommunicationType.GRPC;
  }
}
