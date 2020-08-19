package com.quorum.tessera.partyinfo.model;

import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Recipient;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface NodeInfoUtil {

    static NodeInfo from(PartyInfo partyInfo, Collection<String> versions) {

        List<Recipient> recipients = partyInfo.getRecipients().stream()
            .map(r -> Recipient.of(r.getKey(),r.getUrl())).collect(Collectors.toList());

        List<com.quorum.tessera.partyinfo.node.Party> parties =
            Stream.concat(partyInfo.getParties().stream()
            .map(Party::getUrl),recipients.stream().map(Recipient::getUrl))
            .map(com.quorum.tessera.partyinfo.node.Party::new)
            .collect(Collectors.toList());

        return NodeInfo.Builder.create()
            .withUrl(partyInfo.getUrl())
            .withRecipients(recipients)
            .withParties(parties)
            .withSupportedApiVersions(versions)
            .build();

    }

}
