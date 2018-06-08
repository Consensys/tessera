package com.github.nexus.config;

import java.util.ArrayList;
import java.util.List;

public class Configuration {

    private List<String> publicKeys;

    private List<String> privateKeys;

    public Configuration() {
        this.publicKeys = new ArrayList<>();
        this.privateKeys = new ArrayList<>();
    }

    public List<String> getPublicKeys() {
        return publicKeys;
    }

    public void setPublicKeys(final List<String> publicKeys) {
        this.publicKeys = publicKeys;
    }

    public List<String> getPrivateKeys() {
        return privateKeys;
    }

    public void setPrivateKeys(final List<String> privateKeys) {
        this.privateKeys = privateKeys;
    }

}
