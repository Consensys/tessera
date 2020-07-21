package com.quorum.tessera.version;

/*
 * Initial default api version that reads manifest.mf spec version
 */
public class DefaultApiVersion implements ApiVersion {
    @Override
    public String getVersion() {
        return ApiVersion.class.getPackage().getSpecificationVersion();
    }
}
