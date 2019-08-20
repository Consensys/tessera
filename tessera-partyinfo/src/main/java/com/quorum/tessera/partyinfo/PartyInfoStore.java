package com.quorum.tessera.partyinfo;

import com.quorum.tessera.admin.ConfigService;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

/** Stores a list of all discovered nodes and public keys */
public class PartyInfoStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoStore.class);

    private final String advertisedUrl;

    private final Map<PublicKey, Recipient> recipients;

    private final Set<Party> parties;

    // TODO: Should pull the URI from the config service, where it may be updated in real time
    @Deprecated
    public PartyInfoStore(URI advertisedUrl) {
        // TODO: remove the extra "/" when we deprecate backwards compatibility
        this.advertisedUrl = URLNormalizer.create().normalize(advertisedUrl.toString());
        this.recipients = new HashMap<>();
        this.parties = new HashSet<>();
        this.parties.add(new Party(this.advertisedUrl));
    }

    public PartyInfoStore(final ConfigService configService) {
        this(configService.getServerUri());
    }

    /**
     * Merge an incoming {@link PartyInfo} into the current one, adding any new keys or parties to the current store
     *
     * @param newInfo the incoming information that may contain new nodes/keys
     */
    public synchronized void store(final PartyInfo newInfo) {

        for (Recipient recipient : newInfo.getRecipients()) {
            recipients.put(recipient.getKey(), recipient);
        }

        parties.addAll(newInfo.getParties());

        // update the sender to have been seen recently
        final Party sender = new Party(newInfo.getUrl());
        sender.setLastContacted(Instant.now());
        parties.remove(sender);
        parties.add(sender);
    }

    /**
     * Fetch a copy of all the currently discovered nodes/keys
     *
     * @return an immutable copy of the current state of the store
     */
    public synchronized PartyInfo getPartyInfo() {
        return new PartyInfo(
                advertisedUrl,
                unmodifiableSet(new HashSet<>(recipients.values())),
                unmodifiableSet(new HashSet<>(parties)));
    }

    public synchronized PartyInfo removeRecipient(final String uri) {
        recipients.entrySet().stream()
                .filter(e -> uri.startsWith(e.getValue().getUrl()))
                .map(Map.Entry::getKey)
                .findFirst()
                .ifPresent(recipients::remove);

        LOGGER.info("Removed recipient {} from local PartyInfo store", uri);

        return this.getPartyInfo();
    }
}
