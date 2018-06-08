package com.github.nexus.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PartyInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfo.class);
    
    private final String url;

    private List<Recipient> recipients;

    private List<Party> parties;

    protected PartyInfo(String url, List<Recipient> recipients, List<Party> parties) {
        this.url = url;
        this.recipients = recipients;
        this.parties = parties;
    }

    public String getUrl() {
        return url;
    }

    public List<Recipient> getRecipients() {
        return recipients;
    }

    public List<Party> getParties() {
        return parties;
    }


}
