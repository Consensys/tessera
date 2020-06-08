package com.quorum.tessera.partyinfo;

import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.KeyNotFoundException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.quorum.tessera.partyinfo.PartyInfoServiceUtil.validateKeysToUrls;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

public class PartyInfoServiceImpl implements PartyInfoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoServiceImpl.class);

    private final PartyInfoStore partyInfoStore;

    private final Enclave enclave;

    private final PayloadPublisher payloadPublisher;

    private final KnownPeerCheckerFactory knownPeerCheckerFactory;

    protected PartyInfoServiceImpl(
            final PartyInfoStore partyInfoStore,
            final Enclave enclave,
            final PayloadPublisher payloadPublisher,
            final KnownPeerCheckerFactory knownPeerCheckerFactory) {
        this.partyInfoStore = Objects.requireNonNull(partyInfoStore,"partyInfoStore is required");
        this.enclave = Objects.requireNonNull(enclave,"enclave is required");
        this.payloadPublisher = Objects.requireNonNull(payloadPublisher,"payloadPublisher is required");
        this.knownPeerCheckerFactory = Objects.requireNonNull(knownPeerCheckerFactory,"knownPeerCheckerFactory is required");
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
                        .map(key -> new Recipient(key, advertisedUrl))
                        .collect(toSet());

        PartyInfo partyInfo = new PartyInfo(advertisedUrl, ourKeys, initialParties);
        partyInfoStore.store(partyInfo);
        LOGGER.debug("Populated party info store {}", partyInfo);
    }

    @Override
    public PartyInfo getPartyInfo() {
        return partyInfoStore.getPartyInfo();
    }

    @Override
    public PartyInfo updatePartyInfo(final PartyInfo partyInfo) {

        final RuntimeContext runtimeContext = RuntimeContext.getInstance();

        if (!runtimeContext.isRemoteKeyValidation()) {
            final PartyInfo existingPartyInfo = this.getPartyInfo();

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

        partyInfoStore.store(new PartyInfo(partyInfo.getUrl(), knownRecipients, parties));

        return this.getPartyInfo();
    }

    @Override
    public PartyInfo removeRecipient(String uri) {
        return partyInfoStore.removeRecipient(uri);
    }

    @Override
    public void publishPayload(final EncodedPayload payload, final PublicKey recipientKey) {

        if (enclave.getPublicKeys().contains(recipientKey)) {
            // we are trying to send something to ourselves - don't do it
            LOGGER.debug(
                    "Trying to send message to ourselves with key {}, not publishing", recipientKey.encodeToBase64());
            return;
        }

        final Recipient retrievedRecipientFromStore =
                partyInfoStore.getPartyInfo().getRecipients().stream()
                        .filter(recipient -> recipientKey.equals(recipient.getKey()))
                        .findAny()
                        .orElseThrow(
                                () ->
                                        new KeyNotFoundException(
                                                "Recipient not found for key: " + recipientKey.encodeToBase64()));

        final String targetUrl = retrievedRecipientFromStore.getUrl();

        LOGGER.info("Publishing message to {}", targetUrl);

        payloadPublisher.publishPayload(payload, targetUrl);

        LOGGER.info("Published to {}", targetUrl);
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
                this.enclave.getPublicKeys().stream().map(key -> new Recipient(key, advertisedUrl)).collect(toSet());

        // add to store
        this.partyInfoStore.store(new PartyInfo(advertisedUrl, ourKeys, emptySet()));
    }
}
