package com.quorum.tessera.partyinfo;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;

import java.net.URI;
import java.util.Objects;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

/**
 * Fetches local public keys from the Enclave and adds them to the local store. This is useful when the Enclave is
 * remote and can restart with new keys independently of the Transaction Manager
 */
public class EnclaveKeySynchroniser implements Runnable {

    private final Enclave enclave;

    private PartyInfoStore partyInfoStore;

    private final String advertisedUrl;

    public EnclaveKeySynchroniser(final Enclave enclave, final PartyInfoStore partyInfoStore, URI advertisedUrl) {
        this.enclave = Objects.requireNonNull(enclave);
        this.partyInfoStore = Objects.requireNonNull(partyInfoStore);
        this.advertisedUrl = URLNormalizer.create().normalize(advertisedUrl.toString());
    }

    @Override
    public void run() {

        // fetch keys and create recipients
        final Set<Recipient> ourKeys =
                this.enclave.getPublicKeys().stream()
                        .map(key -> new Recipient(key, this.advertisedUrl))
                        .collect(toSet());

        // add to store
        this.partyInfoStore.store(new PartyInfo(this.advertisedUrl, ourKeys, emptySet()));
    }
}
