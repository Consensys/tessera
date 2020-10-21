package com.quorum.tessera.api;

public class MockVersion implements Version {

    public static final String VERSION = "MOCK";

    @Override
    public String version() {
        return VERSION;
    }
}
