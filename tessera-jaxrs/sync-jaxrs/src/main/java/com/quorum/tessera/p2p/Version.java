package com.quorum.tessera.p2p;

public class Version implements com.quorum.tessera.api.Version {
    @Override
    public String version() {
        return getClass().getPackage().getSpecificationVersion();
    }
}
