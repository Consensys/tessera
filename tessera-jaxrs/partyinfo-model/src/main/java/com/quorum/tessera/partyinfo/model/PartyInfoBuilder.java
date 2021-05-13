package com.quorum.tessera.partyinfo.model;

import com.quorum.tessera.discovery.NodeUri;
import com.quorum.tessera.encryption.PublicKey;
import java.util.*;
import java.util.stream.Collectors;

public class PartyInfoBuilder {

  private PartyInfoBuilder() {}

  private String uri;

  private Map<PublicKey, String> recipients = new HashMap();

  public static PartyInfoBuilder create() {
    return new PartyInfoBuilder();
  }

  public PartyInfoBuilder withUri(String uri) {
    this.uri = uri;
    return this;
  }

  public PartyInfoBuilder withRecipients(Map<PublicKey, String> recipients) {
    this.recipients.putAll(recipients);
    return this;
  }

  public PartyInfo build() {

    Objects.requireNonNull(uri);
    Objects.requireNonNull(recipients);

    String formattedUri = NodeUri.create(uri).asString();
    Set<Recipient> recipientSet =
        recipients.entrySet().stream()
            .filter(e -> e.getValue().equalsIgnoreCase(uri))
            .map(e -> Recipient.of(e.getKey(), NodeUri.create(e.getValue()).asString()))
            .collect(Collectors.toUnmodifiableSet());

    Set<Party> partySet =
        recipients.values().stream().map(Party::new).collect(Collectors.toUnmodifiableSet());

    return new PartyInfo(formattedUri, recipientSet, partySet);
  }
}
