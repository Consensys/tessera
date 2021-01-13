package com.quorum.tessera.api;

public class PrivacyGroupDeleteRequest {

    private String privacyGroupId;
    private String from;

    public String getPrivacyGroupId() {
        return privacyGroupId;
    }

    public void setPrivacyGroupId(String privacyGroupId) {
        this.privacyGroupId = privacyGroupId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
