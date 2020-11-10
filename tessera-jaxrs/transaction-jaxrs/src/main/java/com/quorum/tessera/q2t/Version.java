package com.quorum.tessera.q2t;

public class Version implements com.quorum.tessera.api.Version {
    @Override
    public String version() {
        return getClass().getPackage().getSpecificationVersion();
    }
}
