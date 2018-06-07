package com.github.nexus.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class PartyInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfo.class);
    
    private final String url;

    private final List<Recipient> recipient;

    private final List<Party> parties;

    protected PartyInfo(String url, Recipient[] recipient, Party[] party) {
        this.url = url;
        this.recipient = Arrays.asList(recipient);
        this.parties = Arrays.asList(party);
    }

    public String getUrl() {
        return url;
    }

    public List<Recipient> getRecipients() {
        return recipient;
    }

    public List<Party> getParties() {
        return parties;
    }


}
