package com.quorum.tessera.partyinfo;

import com.quorum.tessera.encryption.KeyNotFoundException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Instant;
import java.util.*;

public class PartyInfoStoreImpl implements PartyInfoStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoStore.class);

    private final String advertisedUrl;

    private final Map<PublicKey, Recipient> recipients;

    private final Set<Party> parties;

    private final Map<Party, VersionInfo> versionInfos;

    protected PartyInfoStoreImpl(URI advertisedUrl) {
        // TODO: remove the extra "/" when we deprecate backwards compatibility
        this.advertisedUrl = URLNormalizer.create().normalize(advertisedUrl.toString());
        this.recipients = new HashMap<>();
        this.parties = new HashSet<>();
        this.parties.add(new Party(this.advertisedUrl));
        this.versionInfos = new HashMap<>();
    }

    /**
     * Merge an incoming {@link NodeInfo} into the current one,
     * adding any new keys or parties to the current store,
     * together with the versioninfo of the sending party
     * @param newInfo the incoming information that may contain new nodes/keys and its versioninfo
     */
    public synchronized void store(final NodeInfo newInfo) {

        for (Recipient recipient : newInfo.getRecipients()) {
            recipients.put(recipient.getKey(), recipient);
        }

        parties.addAll(newInfo.getParties());

        // update the sender to have been seen recently
        final Party sender = new Party(newInfo.getUrl());
        sender.setLastContacted(Instant.now());
        parties.remove(sender);
        parties.add(sender);

        if (Objects.nonNull(newInfo.supportedApiVersions())) {
            versionInfos.put(sender, VersionInfo.from(newInfo.supportedApiVersions()));
        }
    }

    /**
     * Fetch a copy of all the currently discovered nodes/keys
     *
     * @return an immutable copy of the current state of the store
     */
    public synchronized NodeInfo getPartyInfo() {
        return NodeInfo.Builder.create()
            .withUrl(advertisedUrl)
            .withRecipients(recipients.values())
            .withParties(parties)
            .build();

        //new PartyInfo(advertisedUrl, Set.copyOf(recipients.values()), Set.copyOf(parties));
    }

    public synchronized NodeInfo removeRecipient(final String uri) {
        recipients.entrySet().stream()
            .filter(e -> uri.startsWith(e.getValue().getUrl()))
            .map(Map.Entry::getKey)
            .findFirst()
            .ifPresent(recipients::remove);

        LOGGER.info("Removed recipient {} from local PartyInfo store", uri);

        return this.getPartyInfo();
    }

    public Recipient findRecipientByPublicKey(PublicKey key) {
        LOGGER.debug("Find key {}", key);
        Optional<Recipient> recipient = Optional.ofNullable(recipients.get(key));

        if (!recipient.isPresent()) {
            LOGGER.warn("No recipient found for key {}", key);
        }

        return recipient.orElseThrow(() -> new KeyNotFoundException(key.encodeToBase64() + " not found"));
    }

    public String getAdvertisedUrl() {
        return advertisedUrl;
    }

    @Override
    public synchronized VersionInfo getVersionInfo(Party party) {
        return versionInfos.get(party);
    }
}
