package com.quorum.tessera.partyinfo;

import com.quorum.tessera.admin.ConfigService;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.KeyNotFoundException;
import com.quorum.tessera.encryption.PublicKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

public class PartyInfoServiceImpl implements PartyInfoService {

    private final PartyInfoStore partyInfoStore;

    private final ConfigService configService;

    private final Enclave enclave;

    private final PayloadPublisher payloadPublisher;

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoServiceImpl.class);

    public PartyInfoServiceImpl(ConfigService configService, final Enclave enclave, PayloadPublisher payloadPublisher) {
        this(new PartyInfoStore(configService.getServerUri()), configService, enclave, payloadPublisher);
    }

    protected PartyInfoServiceImpl(
            final PartyInfoStore partyInfoStore,
            final ConfigService configService,
            final Enclave enclave,
            PayloadPublisher payloadPublisher) {
        this.partyInfoStore = Objects.requireNonNull(partyInfoStore);
        this.configService = Objects.requireNonNull(configService);
        this.enclave = Objects.requireNonNull(enclave);
        this.payloadPublisher = payloadPublisher;
        final String advertisedUrl = URLNormalizer.create().normalize(configService.getServerUri().toString());

        final Set<Party> initialParties =
                configService.getPeers().stream().map(Peer::getUrl).map(Party::new).collect(toSet());

        final Set<Recipient> ourKeys =
                enclave.getPublicKeys().stream()
                        .map(key -> PublicKey.from(key.getKeyBytes()))
                        .map(key -> new Recipient(key, advertisedUrl))
                        .collect(toSet());

        partyInfoStore.store(new PartyInfo(advertisedUrl, ourKeys, initialParties));
    }

    @Override
    public PartyInfo getPartyInfo() {
        return partyInfoStore.getPartyInfo();
    }

    @Override
    public PartyInfo updatePartyInfo(final PartyInfo partyInfo) {

        if (!configService.isDisablePeerDiscovery()) {
            // auto-discovery is on, we can accept all input to us
            this.partyInfoStore.store(partyInfo);
            return partyInfoStore.getPartyInfo();
        }

        // auto-discovery is off
        final Set<String> peerUrls = configService.getPeers().stream().map(Peer::getUrl).collect(Collectors.toSet());

        LOGGER.debug("Known peers: {}", peerUrls);

        // check the caller is allowed to update our party info, which it can do
        // if it one of our known peers
        final String incomingUrl = partyInfo.getUrl();

        // TODO: should we just check peer is the same or with +"/", instead of just starts with?
        if (peerUrls.stream().noneMatch(incomingUrl::startsWith)) {
            final String message = String.format("Peer %s not found in known peer list", partyInfo.getUrl());
            throw new AutoDiscoveryDisabledException(message);
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
}
