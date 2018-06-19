package com.github.nexus.api.model;

import io.swagger.annotations.ApiModel;
import javax.validation.constraints.NotNull;

@ApiModel
public class SendRequest{

    @NotNull
    private String payload;

    private String from;

    private String[] to;

    public String getPayload() {
        return payload;
    }

    public void setPayload(final String payload) {
        this.payload = payload;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(final String from) {
        this.from = from;
    }

    public String[] getTo() {
        if (to == null) {
            return new String[]{};
        }

        return to;
    }

    public void setTo(final String[] to) {
        this.to = to;
    }
}
