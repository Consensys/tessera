package com.github.nexus.node;

import com.github.nexus.enclave.keys.model.Key;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PartyInfoServiceImpl implements PartyInfoService {

    private PartyInfoStore partyInfoStore = PartyInfoStore.INSTANCE;

    @Override
    public void initPartyInfo(String url, String[] otherNodes) {
        List<Party> parties = Stream.of(otherNodes)
            .map(node -> new Party(node)).collect(Collectors.toList());

        partyInfoStore.store(new PartyInfo(url, new ArrayList<Recipient>(), parties));
    }

    @Override
    public void registerPublicKeys(Key[] publicKeys) {
        Arrays.asList(publicKeys).forEach(key ->{
            Recipient recipient = new Recipient(key);
            partyInfoStore.getPartyInfo().getRecipients().add(recipient);
        });
    }

    @Override
    public PartyInfo getPartyInfo() {
        return partyInfoStore.getPartyInfo();
    }

    @Override
    public PartyInfo updatePartyInfo(PartyInfo partyInfo) {

        partyInfoStore.store(partyInfo);

        return partyInfoStore.getPartyInfo();
    }
}
