package com.quorum.tessera.partyinfo;

import com.quorum.tessera.admin.ConfigService;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;
import javax.annotation.PostConstruct;

public class PartyInfoServiceImpl implements PartyInfoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoServiceImpl.class);

    private final PartyInfoStore partyInfoStore;

    private final ConfigService configService;

    private final Enclave enclave;

    private final PartyInfoValidator partyInfoValidator;

    private final PayloadEncoder payloadEncoder;

    public PartyInfoServiceImpl(
            PartyInfoStore partyInfoStore,
            ConfigService configService,
            Enclave enclave,
            PartyInfoValidator partyInfoValidator,
            PayloadEncoder payloadEncoder) {
        this.partyInfoStore = partyInfoStore;
        this.configService = configService;
        this.enclave = enclave;
        this.partyInfoValidator = partyInfoValidator;
        this.payloadEncoder = payloadEncoder;
    }

    public PartyInfoServiceImpl(
            PartyInfoStore partyInfoStore, final ConfigService configService, final Enclave enclave) {
        this(partyInfoStore, configService, enclave, new PartyInfoValidatorImpl(enclave), PayloadEncoder.create());
    }

    @PostConstruct
    public void onConstruct() {
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

        if (!configService.featureToggles().isEnableRemoteKeyValidation()) {
            final PartyInfo existingPartyInfo = this.getPartyInfo();

            if (!this.validateKeysToUrls(existingPartyInfo, partyInfo)) {
                LOGGER.warn(
                        "Attempt is being made to update existing key with new url. Terminating party info update.");
                return this.getPartyInfo();
            }
        }

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
        LOGGER.debug("Removing recipient {} from store", uri);
        return partyInfoStore.removeRecipient(uri);
    }

    boolean validateKeysToUrls(final PartyInfo existingPartyInfo, final PartyInfo newPartyInfo) {

        final Map<PublicKey, String> existingRecipientKeyUrlMap =
                existingPartyInfo.getRecipients().stream()
                        .collect(Collectors.toMap(Recipient::getKey, Recipient::getUrl));

        final Map<PublicKey, String> newRecipientKeyUrlMap =
                newPartyInfo.getRecipients().stream().collect(Collectors.toMap(Recipient::getKey, Recipient::getUrl));

        for (final Map.Entry<PublicKey, String> entry : newRecipientKeyUrlMap.entrySet()) {
            final PublicKey key = entry.getKey();

            if (existingRecipientKeyUrlMap.containsKey(key)) {
                String existingUrl = existingRecipientKeyUrlMap.get(key);
                String newUrl = entry.getValue();
                if (!existingUrl.equalsIgnoreCase(newUrl)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public Set<Recipient> validateAndExtractValidRecipients(
            PartyInfo partyInfo, PartyInfoValidatorCallback partyInfoValidatorCallback) {
        return partyInfoValidator.validateAndFetchValidRecipients(partyInfo, partyInfoValidatorCallback);
    }

    @Override
    public byte[] unencryptSampleData(byte[] payloadData) {

        final EncodedPayload payload = payloadEncoder.decode(payloadData);

        final PublicKey mykey = payload.getRecipientKeys().iterator().next();

        return enclave.unencryptTransaction(payload, mykey);
    }
}
