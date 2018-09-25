package com.quorum.tessera.node;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.core.config.ConfigService;
import com.quorum.tessera.key.KeyManager;
import com.quorum.tessera.key.exception.KeyNotFoundException;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.node.model.Party;
import com.quorum.tessera.node.model.PartyInfo;
import com.quorum.tessera.node.model.Recipient;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PartyInfoServiceImpl implements PartyInfoService {

    private final PartyInfoStore partyInfoStore;

    private final ConfigService configService;
        
    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoServiceImpl.class);
    
    public PartyInfoServiceImpl(final PartyInfoStore partyInfoStore,
                                final ConfigService configService,
                                final KeyManager keyManager) {
        this.partyInfoStore = Objects.requireNonNull(partyInfoStore);
        this.configService = Objects.requireNonNull(configService);
        
        final Config configuration = configService.getConfig();
        final String advertisedUrl = configuration.getServerConfig().getServerUri().toString();

        final Set<Party> initialParties = configuration
            .getPeers()
            .stream()
            .map(Peer::getUrl)
            .map(Party::new)
            .collect(toSet());

        final Set<Recipient> ourKeys = keyManager
            .getPublicKeys()
            .stream()
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
        
        if(configService.getConfig().isDisablePeerDiscovery()) {
            
            PartyInfo currentPartyInfo = getPartyInfo();
            
            if(!Objects.equals(currentPartyInfo.getParties(), partyInfo.getParties())) {
                final String message = String.format("Parties found in party info from %s are different.", partyInfo.getUrl());
                throw new AutoDiscoveryDisabledException(message);
            }
            
            Predicate<String> matchUrl = u -> Objects.equals(u, partyInfo.getUrl());
            
            if(!currentPartyInfo.getParties().stream()
                    .map(Party::getUrl)
                    .anyMatch(matchUrl)) {
                final String message = String.format("Peer %s not found in known peer list", partyInfo.getUrl());
                throw new AutoDiscoveryDisabledException(message);
            }

        }

        partyInfoStore.store(partyInfo);

        return this.getPartyInfo();
    }

    @Override
    public String getURLFromRecipientKey(final Key key) {

        final Recipient retrievedRecipientFromStore = partyInfoStore
            .getPartyInfo()
            .getRecipients()
            .stream()
            .filter(recipient -> key.equals(recipient.getKey()))
            .findAny()
            .orElseThrow(() -> new KeyNotFoundException("Recipient not found"));

        return retrievedRecipientFromStore.getUrl();
    }

    @Override
    public Set<Party> findUnsavedParties(final PartyInfo partyInfoWithUnsavedRecipients) {
        final Set<Party> knownHosts = this.getPartyInfo().getParties();

        final Set<Party> incomingRecipients = new HashSet<>(partyInfoWithUnsavedRecipients.getParties());
        incomingRecipients.removeAll(knownHosts);

        return Collections.unmodifiableSet(incomingRecipients);
    }

}
