package com.quorum.tessera.api;

public class Version {

    public static String getVersion() {
        return Version.class.getPackage().getSpecificationVersion();
    }
}
