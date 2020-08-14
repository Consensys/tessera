package com.quorum.tessera.partyinfo;

import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Party;
import com.quorum.tessera.partyinfo.node.Recipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.quorum.tessera.partyinfo.PartyInfoServiceUtil.validateKeysToUrls;
import static java.util.stream.Collectors.toSet;

public class PartyInfoServiceImpl implements PartyInfoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoServiceImpl.class);

    private final PartyInfoStore partyInfoStore;

    private final Enclave enclave;

    private final KnownPeerCheckerFactory knownPeerCheckerFactory;

    protected PartyInfoServiceImpl(
            final PartyInfoStore partyInfoStore,
            final Enclave enclave,
            final KnownPeerCheckerFactory knownPeerCheckerFactory) {
        this.partyInfoStore = Objects.requireNonNull(partyInfoStore, "partyInfoStore is required");
        this.enclave = Objects.requireNonNull(enclave, "enclave is required");
        this.knownPeerCheckerFactory =
                Objects.requireNonNull(knownPeerCheckerFactory, "knownPeerCheckerFactory is required");
    }

    @Override
    public void populateStore() {
        LOGGER.debug("Populating store");
        RuntimeContext runtimeContext = RuntimeContext.getInstance();

        final String partyStoreUrl = partyInfoStore.getPartyInfo().getUrl();
        final String advertisedUrl = URLNormalizer.create().normalize(partyStoreUrl);
        LOGGER.debug("Populate party info store for {}", advertisedUrl);

        final Set<Party> initialParties =
                runtimeContext.getPeers().stream()
                        .map(Objects::toString)
                        .peek(o -> LOGGER.debug("Party {}", o))
                        .map(Party::new)
                        .collect(toSet());

        LOGGER.debug("{} peers found. ", initialParties.size());

        final Set<Recipient> ourKeys =
                enclave.getPublicKeys().stream()
                        .peek(o -> LOGGER.debug("{}", o))
                        .map(key -> Recipient.of(key, advertisedUrl))
                        .collect(toSet());

        NodeInfo nodeInfo =
                NodeInfo.Builder.create()
                        .withParties(initialParties)
                        .withRecipients(ourKeys)
                        .withUrl(advertisedUrl)
                        .build();
        partyInfoStore.store(nodeInfo);
        LOGGER.debug("Populated party info store {}", nodeInfo);
    }

    @Override
    public NodeInfo getPartyInfo() {
        return partyInfoStore.getPartyInfo();
    }

    @Override
    public NodeInfo updatePartyInfo(final NodeInfo partyInfo) {

        final RuntimeContext runtimeContext = RuntimeContext.getInstance();

        if (!runtimeContext.isRemoteKeyValidation()) {
            final NodeInfo existingPartyInfo = this.getPartyInfo();

            if (!validateKeysToUrls(existingPartyInfo, partyInfo)) {
                LOGGER.warn(
                        "Attempt is being made to update existing key with new url. "
                                + "Please switch on remote key validation to avoid a security breach.");
            }
        }

        if (!runtimeContext.isDisablePeerDiscovery()) {
            // auto-discovery is on, we can accept all input to us
            this.partyInfoStore.store(partyInfo);
            return this.getPartyInfo();
        }

        // auto-discovery is off
        final Set<String> peerUrls =
                runtimeContext.getPeers().stream().map(Objects::toString).collect(Collectors.toSet());

        LOGGER.debug("Known peers: {}", peerUrls);

        // check the caller is allowed to update our party info, which it can do
        // if it one of our known peers
        final String incomingUrl = partyInfo.getUrl();

        final KnownPeerChecker knownPeerChecker = knownPeerCheckerFactory.create(peerUrls);
        if (!knownPeerChecker.isKnown(incomingUrl)) {
            throw new AutoDiscoveryDisabledException(String.format("%s is not a known peer", incomingUrl));
        }

        // filter out all keys that aren't from that node
        final Set<Recipient> knownRecipients =
                partyInfo.getRecipients().stream()
                        .filter(recipient -> Objects.equals(recipient.getUrl(), incomingUrl))
                        .collect(Collectors.toSet());

        // TODO: instead of adding the peers every time, if a new peer is added at runtime then this should be added
        // separately
        final Set<Party> parties = peerUrls.stream().map(Party::new).collect(toSet());

        final NodeInfo updated =
                NodeInfo.Builder.create()
                        .withUrl(incomingUrl)
                        .withParties(parties)
                        .withRecipients(knownRecipients)
                        .withSupportedApiVersions(partyInfo.supportedApiVersions())
                        .build();

        partyInfoStore.store(updated);

        return this.getPartyInfo();
    }

    @Override
    public NodeInfo removeRecipient(String uri) {
        return partyInfoStore.removeRecipient(uri);
    }

    /**
     * Fetches local public keys from the Enclave and adds them to the local store. This is useful when the Enclave is
     * remote and can restart with new keys independently of the Transaction Manager
     */
    @Override
    public void syncKeys() {

        final String advertisedUrl = partyInfoStore.getAdvertisedUrl();

        // fetch keys and create recipients
        final Set<Recipient> ourKeys =
                this.enclave.getPublicKeys().stream().map(key -> Recipient.of(key, advertisedUrl)).collect(toSet());

        // add to store
        final NodeInfo newInfo = NodeInfo.Builder.create().withUrl(advertisedUrl).withRecipients(ourKeys).build();
        this.partyInfoStore.store(newInfo);
    }
}
