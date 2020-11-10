package com.quorum.tessera.thirdparty;

public class Version implements com.quorum.tessera.api.Version {
    @Override
    public String version() {
        return getClass().getPackage().getSpecificationVersion();
    }
}
