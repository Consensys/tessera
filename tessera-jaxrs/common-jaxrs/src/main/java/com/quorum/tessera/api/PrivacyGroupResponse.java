package com.quorum.tessera.api;

public class PrivacyGroupResponse {

    private String privacyGroupId;
    private String name;
    private String description;
    private String type;
    private String[] members;

    public PrivacyGroupResponse(String privacyGroupId, String name, String description, String type, String[] members) {
        this.privacyGroupId = privacyGroupId;
        this.name = name;
        this.description = description;
        this.type = type;
        this.members = members;
    }

    public PrivacyGroupResponse() {}

    public String getPrivacyGroupId() {
        return privacyGroupId;
    }

    public void setPrivacyGroupId(String privacyGroupId) {
        this.privacyGroupId = privacyGroupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String[] getMembers() {
        return members;
    }

    public void setMembers(String[] members) {
        this.members = members;
    }
}
