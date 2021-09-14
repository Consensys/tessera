package com.quorum.tessera.api.exception;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.transaction.publish.NodeOfflineException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeOfflineExceptionMapper implements ExceptionMapper<NodeOfflineException> {

  private static final Logger LOGGER = LoggerFactory.getLogger(NodeOfflineExceptionMapper.class);

  private final Discovery discovery;

  public NodeOfflineExceptionMapper() {
    this(Discovery.create());
  }

  protected NodeOfflineExceptionMapper(Discovery discovery) {
    this.discovery = Objects.requireNonNull(discovery);
  }

  @Override
  public Response toResponse(NodeOfflineException exception) {
    LOGGER.warn("{} is unavailable assuming disconnection", exception.getUri());
    discovery.onDisconnect(exception.getUri());
    return Response.status(Response.Status.GONE.getStatusCode(), exception.getMessage()).build();
  }
}
