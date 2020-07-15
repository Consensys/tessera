package com.quorum.tessera.server.http;

import com.quorum.tessera.version.VersionInfo;

public class MockVersionInfo implements VersionInfo {

    public static final String CURRENT_VERSION = "1";

    public static final String PREVIOUS_VERSION = "1";

    @Override
    public String currentVersion() {
        return CURRENT_VERSION;
    }

    @Override
    public String previousVersion() {
        return PREVIOUS_VERSION;
    }
}
