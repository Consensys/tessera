package com.quorum.tessera.transaction.publish;

import java.net.URI;
import java.util.Objects;

public class NodeOfflineException extends RuntimeException {

  private URI uri;

  public NodeOfflineException(URI uri) {
    super(String.format("Connection error while communicating with %s", Objects.toString(uri)));
    this.uri = uri;
  }

  public URI getUri() {
    return uri;
  }
}
