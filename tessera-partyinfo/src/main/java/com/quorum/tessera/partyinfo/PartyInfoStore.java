package com.quorum.tessera.partyinfo;


import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;

import java.net.URI;

public interface PartyInfoStore {


    static PartyInfoStore create() {

        return ServiceLoaderUtil.load(PartyInfoStore.class)
            .orElseGet(() -> {
            RuntimeContext runtimeContext = RuntimeContext.getInstance();
            URI url = runtimeContext.getP2pServerUri();
            return new PartyInfoStoreImpl(url);
        });

    }

    PartyInfo getPartyInfo();

    void store(PartyInfo incomingInfo);

    Recipient findRecipientByPublicKey(PublicKey from);

    String getAdvertisedUrl();

    PartyInfo removeRecipient(String s);
}


