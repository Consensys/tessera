package com.quorum.tessera.discovery;

import com.quorum.tessera.encryption.PublicKey;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ActiveNode {

  private final Set<PublicKey> keys;

  private final NodeUri uri;

  private final Set<String> supportedVersions;

  public Set<PublicKey> getKeys() {
    return keys;
  }

  public NodeUri getUri() {
    return uri;
  }

  public Set<String> getSupportedVersions() {
    return supportedVersions;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ActiveNode that = (ActiveNode) o;
    return uri.equals(that.uri);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uri);
  }

  @Override
  public String toString() {
    return "ActiveNode{"
        + "keys="
        + keys
        + ", uri="
        + uri
        + ", supportedVersions="
        + supportedVersions
        + '}';
  }

  private ActiveNode(NodeUri uri, Set<PublicKey> keys, Set<String> supportedVersions) {
    this.keys = keys;
    this.uri = uri;
    this.supportedVersions = supportedVersions;
  }

  public static class Builder {

    private NodeUri nodeUri;

    private List<PublicKey> keys = List.of();

    private List<String> supportedVersions = List.of();

    public static Builder create() {
      return new Builder();
    }

    public static Builder from(ActiveNode activeNode) {
      return create()
          .withUri(activeNode.getUri())
          .withSupportedVersions(activeNode.getSupportedVersions())
          .withKeys(activeNode.getKeys());
    }

    private Builder() {}

    public Builder withUri(NodeUri nodeUri) {
      this.nodeUri = nodeUri;
      return this;
    }

    public Builder withKeys(Collection<PublicKey> keys) {
      this.keys = List.copyOf(keys);
      return this;
    }

    public Builder withSupportedVersions(Collection<String> supportedVersions) {
      this.supportedVersions = List.copyOf(supportedVersions);
      return this;
    }

    public ActiveNode build() {
      Objects.requireNonNull(nodeUri, "Node uri is required");
      Objects.requireNonNull(keys, "keys are required");
      return new ActiveNode(nodeUri, Set.copyOf(keys), Set.copyOf(supportedVersions));
    }
  }
}
