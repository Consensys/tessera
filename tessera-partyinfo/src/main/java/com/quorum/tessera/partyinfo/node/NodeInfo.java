package com.quorum.tessera.partyinfo.node;

import com.quorum.tessera.encryption.PublicKey;
import java.util.*;
import java.util.stream.Collectors;

public interface NodeInfo {

  default Map<PublicKey, String> getRecipientsAsMap() {
    return getRecipients().stream()
        .collect(Collectors.toUnmodifiableMap(Recipient::getKey, Recipient::getUrl));
  }

  Set<Recipient> getRecipients();

  Set<String> supportedApiVersions();

  String getUrl();

  class Builder {

    private String url;

    private final Set<Recipient> recipients = new HashSet<>();

    private Set<String> supportedApiVersions = new HashSet<>();

    public Builder withRecipients(Collection<Recipient> recipients) {
      this.recipients.addAll(recipients);
      return this;
    }

    public Builder withSupportedApiVersions(Collection<String> supportedApiVersions) {
      if (Objects.nonNull(supportedApiVersions)) {
        this.supportedApiVersions = Set.copyOf(supportedApiVersions);
      }
      return this;
    }

    public NodeInfo build() {

      Objects.requireNonNull(url, "URL is required");

      return new NodeInfo() {

        @Override
        public Set<Recipient> getRecipients() {
          return Set.copyOf(recipients);
        }

        @Override
        public Set<String> supportedApiVersions() {
          return Set.copyOf(supportedApiVersions);
        }

        @Override
        public String getUrl() {
          return url;
        }

        @Override
        public String toString() {
          return String.format("NodeInfo[url: %s ,recipients: %s]", url, recipients);
        }
      };
    }

    public static Builder create() {
      return new Builder() {};
    }

    private Builder() {}

    public Builder withUrl(String url) {
      this.url = url;
      return this;
    }
  }
}
