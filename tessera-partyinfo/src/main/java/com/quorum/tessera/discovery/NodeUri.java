package com.quorum.tessera.discovery;

import com.quorum.tessera.partyinfo.URLNormalizer;
import java.net.URI;
import java.util.Objects;

public class NodeUri {

  private final String value;

  private NodeUri(String value) {
    this.value = URLNormalizer.create().normalize(Objects.requireNonNull(value, "URI is required"));
  }

  public static NodeUri create(String uri) {
    return new NodeUri(uri);
  }

  public static NodeUri create(URI uri) {
    return new NodeUri(Objects.toString(uri));
  }

  public String asString() {
    return value;
  }

  public URI asURI() {
    return URI.create(value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    NodeUri nodeUri = (NodeUri) o;
    return value.equals(nodeUri.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return String.format("NodeUri{%s}", value);
  }
}
