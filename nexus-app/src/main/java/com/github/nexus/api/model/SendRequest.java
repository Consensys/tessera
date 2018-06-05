package com.github.nexus.api.model;

import javax.validation.constraints.NotNull;

public class SendRequest{

    @NotNull
    private String payload;
    private String from;
    @NotNull
    private String[] to;

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String[] getTo() {
        return to;
    }

    public void setTo(String[] to) {
        this.to = to;
    }
}
