package net.consensys.tessera.b2t.model;

import java.util.Optional;

public class PrivacyGroupRequest {

    private String[] addresses;
    private String from;
    private String name;
    private String description;
    private String seed;

    public PrivacyGroupRequest() {}

    public String[] getAddresses() {
        return addresses;
    }

    public void setAddresses(String[] addresses) {
        this.addresses = addresses;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
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

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }
}
