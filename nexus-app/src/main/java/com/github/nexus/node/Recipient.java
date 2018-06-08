package com.github.nexus.node;

import com.github.nexus.enclave.keys.model.Key;

public class Recipient {

    private Key key;
    private String url;

    public Recipient(Key key, String url) {
        this.key = key;
        this.url = url;
    }

    public Recipient(Key key) {
        this.key = key;
    }

    public Key getKey() {
        return key;
    }

    public String getUrl() {
        return url;
    }
}
