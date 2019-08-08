package com.quorum.tessera.partyinfo;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface PartyInfoStore {

    Logger LOGGER = LoggerFactory.getLogger(PartyInfoStore.class);

    PartyInfoStore init(URI advertisedUri);

    void store(PartyInfo newInfo);

    PartyInfo getPartyInfo();

    PartyInfo removeRecipient(String uri);

    Recipient findRecipientByPublicKey(PublicKey key);

    void clear();

    static PartyInfoStore create(URI advertisedUrl) {
        // FIXME: URI normalisation needs to be brough under control
        String normalisedUri = URLNormalizer.create().normalize(advertisedUrl.toString());
        return PartyInfoStoreImpl.INSTANCE.init(URI.create(normalisedUri));
    }

    static PartyInfoStore get() {
        return PartyInfoStoreImpl.INSTANCE;
    }
}
