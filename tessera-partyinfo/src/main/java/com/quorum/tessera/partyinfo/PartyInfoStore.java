package com.quorum.tessera.partyinfo;


import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.model.*;

import java.net.URI;

public interface PartyInfoStore {


    static PartyInfoStore create(URI uri) {
        return ServiceLoaderUtil.load(PartyInfoStore.class)
            .orElse(new PartyInfoStoreImpl(uri));
    }

    PartyInfo getPartyInfo();

    void store(NodeInfo incomingInfo);

    Recipient findRecipientByPublicKey(PublicKey from);

    String getAdvertisedUrl();

    PartyInfo removeRecipient(String s);

    VersionInfo getVersionInfo(Party party);
}


