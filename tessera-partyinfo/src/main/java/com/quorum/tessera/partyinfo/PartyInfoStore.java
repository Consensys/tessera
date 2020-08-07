package com.quorum.tessera.partyinfo;


import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Party;
import com.quorum.tessera.partyinfo.node.Recipient;
import com.quorum.tessera.partyinfo.node.VersionInfo;

import java.net.URI;

public interface PartyInfoStore {


    static PartyInfoStore create(URI uri) {
        return ServiceLoaderUtil.load(PartyInfoStore.class)
            .orElse(new PartyInfoStoreImpl(uri));
    }

    NodeInfo getPartyInfo();

    void store(NodeInfo incomingInfo);

    Recipient findRecipientByPublicKey(PublicKey from);

    String getAdvertisedUrl();

    NodeInfo removeRecipient(String s);

    VersionInfo getVersionInfo(Party party);
}


