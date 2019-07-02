package com.quorum.tessera.data.migration;

import java.io.InputStream;

public class DataEntry {

    private final byte[] key;

    private final InputStream value;

    public DataEntry(final byte[] key, final InputStream value) {
        this.key = key;
        this.value = value;
    }

    public byte[] getKey() {
        return this.key;
    }

    public InputStream getValue() {
        return this.value;
    }
}
