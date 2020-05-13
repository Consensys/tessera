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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.quorum.tessera.partyinfo.PartyInfoServiceUtil.validateKeysToUrls;
import static java.util.stream.Collectors.toSet;

public class PartyInfoServiceImpl implements PartyInfoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoServiceImpl.class);

    private final PartyInfoStore partyInfoStore;

    private final Enclave enclave;

    private final PayloadPublisher payloadPublisher;

    public PartyInfoServiceImpl(final PartyInfoServiceFactory partyInfoServiceFactory) {
        this(
                partyInfoServiceFactory.partyInfoStore(),
                partyInfoServiceFactory.enclave(),
                partyInfoServiceFactory.payloadPublisher());
    }

    protected PartyInfoServiceImpl(
            final PartyInfoStore partyInfoStore, final Enclave enclave, final PayloadPublisher payloadPublisher) {
        this.partyInfoStore = Objects.requireNonNull(partyInfoStore);
        this.enclave = Objects.requireNonNull(enclave);
        this.payloadPublisher = Objects.requireNonNull(payloadPublisher);
    }

    @Override
    public void populateStore() {
        LOGGER.debug("Populating store");
        RuntimeContext runtimeContext = RuntimeContext.getInstance();
        final String advertisedUrl = URLNormalizer.create().normalize(partyInfoStore.getPartyInfo().getUrl());
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

        // TODO: should we just check peer is the same or with +"/", instead of just starts with?
        try {
            if (!isPeer(incomingUrl, peerUrls)) {
                final String message = String.format("Peer %s not found in known peer list", partyInfo.getUrl());
                LOGGER.warn(message);
                throw new AutoDiscoveryDisabledException(message);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("unable to check if sender of partyinfo is a known peer", e);
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

    private static boolean isPeer(String url, Set<String> peers) throws MalformedURLException {
        LOGGER.debug("PartyInfoServiceImpl::isPeer, url: {}", url);
        final URLNormalizer urlNormalizer = URLNormalizer.create();
        for (String peer : peers) {
            LOGGER.debug("peer {}", peer);

            // allow for trailing '/'
            final String normalizedPeer = urlNormalizer.normalize(peer);
            LOGGER.debug("normalizedPeer {}", normalizedPeer);

            if (url.equals(normalizedPeer)) {
                LOGGER.debug("{} string equals {}", url, normalizedPeer);
                return true;
            }
            // if hostname is provided instead IP (or vice versa) for localhost then return true
            if ((url.contains("localhost") || url.contains("127.0.0.1"))
                    && (normalizedPeer.contains("localhost") || normalizedPeer.contains("127.0.0.1"))) {
                LOGGER.debug("{} or {} contain localhost", url, normalizedPeer);
                URL u = new URL(url);
                URL p = new URL(normalizedPeer);
                if (u.equals(p)) {
                    LOGGER.debug("URL::equal = true");
                    return true;
                }
            }
            LOGGER.debug("{} and {} not equal", url, normalizedPeer);
        }
        LOGGER.debug("{} not matched", url);
        return false;
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
}
