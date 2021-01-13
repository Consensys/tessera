package com.quorum.tessera.version;

public class PrivacyGroupVersion implements ApiVersion {

    public static final String API_VERSION_3 = "3.0";

    @Override
    public String getVersion() {
        return API_VERSION_3;
    }
}
