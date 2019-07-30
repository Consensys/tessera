package com.quorum.tessera.partyinfo;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import java.net.URI;

public interface PartyInfoStore {

    void store(PartyInfo newInfo);

    PartyInfo getPartyInfo();

    PartyInfo removeRecipient(String uri);

    Recipient findRecipientByPublicKey(PublicKey key);

    void clear();

    static PartyInfoStore create(URI advertisedUrl) {
        URI uri = URI.create(URLNormalizer.create().normalize(advertisedUrl.toString()));
        return PartyInfoStoreImpl.INSTANCE.init(uri);
    }

    static PartyInfoStore get() {
        return PartyInfoStoreImpl.INSTANCE;
    }
}
