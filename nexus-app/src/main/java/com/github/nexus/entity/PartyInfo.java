package com.github.nexus.entity;

import javax.ws.rs.client.Client;
import java.util.Map;

public class PartyInfo {

    private String url;
    private Map<byte[], String> recipients;
    private Map<String, Boolean> parties;
    private Client client;

    public PartyInfo(String url, Map<byte[], String> recipients, Map<String, Boolean> parties, Client client) {
        this.url = url;
        this.recipients = recipients;
        this.parties = parties;
        this.client = client;
    }
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<byte[], String> getRecipients() {
        return recipients;
    }

    public void setRecipients(Map<byte[], String> recipients) {
        this.recipients = recipients;
    }

    public Map<String, Boolean> getParties() {
        return parties;
    }

    public void setParties(Map<String, Boolean> parties) {
        this.parties = parties;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
