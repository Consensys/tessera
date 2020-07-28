package com.quorum.tessera.partyinfo;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;

import java.net.URI;

public interface PartyInfoStore {


    static PartyInfoStore create(URI uri) {

        ExclusionCache<Recipient> recipientExclusionCache = ExclusionCache.create();
        return ServiceLoaderUtil.load(PartyInfoStore.class)
            .orElse(new PartyInfoStoreImpl(uri,recipientExclusionCache));
    }

    PartyInfo getPartyInfo();

    void store(PartyInfo incomingInfo);

    Recipient findRecipientByPublicKey(PublicKey from);

    String getAdvertisedUrl();

    PartyInfo removeRecipient(String s);

}


