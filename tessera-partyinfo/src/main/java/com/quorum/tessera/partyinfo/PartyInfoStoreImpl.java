package com.quorum.tessera.partyinfo;

import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.encryption.KeyNotFoundException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;

public class PartyInfoStoreImpl implements PartyInfoStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoStore.class);

    private final String advertisedUrl;

    private final Map<PublicKey, Recipient> recipients;

    private final Set<Party> parties;

    private final ExclusionCache<Recipient> exclusionCache;


    private final Map<Party, VersionInfo> versionInfos;

    protected PartyInfoStoreImpl(URI advertisedUrl,ExclusionCache<Recipient> exclusionCache) {
        // TODO: remove the extra "/" when we deprecate backwards compatibility
        this.advertisedUrl = URLNormalizer.create().normalize(advertisedUrl.toString());
        this.recipients = new HashMap<>();
        this.parties = new HashSet<>();
        this.parties.add(new Party(this.advertisedUrl));

        this.exclusionCache = Objects.requireNonNull(exclusionCache);

        this.versionInfos = new HashMap<>();
    }
    /**
     * Merge an incoming {@link NodeInfo} into the current one,
     * adding any new keys or parties to the current store,
     * together with the versioninfo of the sending party
     * @param incomingInfo the incoming information that may contain new nodes/keys and its versioninfo
     */
    public synchronized void store(NodeInfo incomingInfo) {

        final PartyInfo newInfo = incomingInfo.partyInfo();


        RuntimeContext runtimeContext = RuntimeContext.getInstance();
        if(!runtimeContext.isDisablePeerDiscovery()) {
            addRecipient(newInfo.getUrl());

            final Set<String> excludedUrls = new HashSet<>();
            for (Recipient recipient : newInfo.getRecipients()) {
                if(exclusionCache.isExcluded(recipient)) {
                    LOGGER.info("Recipient {} is excluded. Assumed offline",recipient);
                    parties.removeIf(p -> Objects.equals(p.getUrl(),recipient.getUrl()));
                    recipients.remove(recipient.getKey());
                    excludedUrls.add(recipient.getUrl());
                } else {
                    recipients.put(recipient.getKey(), recipient);
                }
            }

            newInfo.getParties().stream()
                .filter(Predicate.not(p -> excludedUrls.contains(p.getUrl())))
                .forEach(parties::add);
        }

        // update the sender to have been seen recently
        final Party sender = new Party(newInfo.getUrl());
        sender.setLastContacted(Instant.now());
        parties.remove(sender);
        parties.add(sender);

        final VersionInfo versionInfo = incomingInfo.versionInfo();
        if (Objects.nonNull(versionInfo)) {
            versionInfos.put(sender, versionInfo);
        }
    }

    /**
     * Fetch a copy of all the currently discovered nodes/keys
     *
     * @return an immutable copy of the current state of the store
     */
    public synchronized PartyInfo getPartyInfo() {
        return new PartyInfo(advertisedUrl, Set.copyOf(recipients.values()), Set.copyOf(parties));
    }



    public synchronized PartyInfo removeRecipient(final String uri) {
        RuntimeContext runtimeContext = RuntimeContext.getInstance();
        if(runtimeContext.isDisablePeerDiscovery()) {
            return this.getPartyInfo();
        }
        recipients.entrySet().stream()
            .filter(e -> uri.startsWith(e.getValue().getUrl()))
            .map(Map.Entry::getKey)
            .findFirst()
            .ifPresent(r -> {
                exclusionCache.exclude(recipients.get(r));
                recipients.remove(r);
                parties.removeIf(p -> uri.startsWith(p.getUrl()));
            });

        LOGGER.info("Removed recipient {} from local PartyInfo store", uri);

        return this.getPartyInfo();
    }

    public synchronized void addRecipient(String recipient) {
        exclusionCache.include(recipient).ifPresent(r -> {
            recipients.put(r.getKey(),r);
            parties.add(new Party(r.getUrl()));
        });

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
