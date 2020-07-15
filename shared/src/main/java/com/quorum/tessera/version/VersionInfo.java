package com.quorum.tessera.version;

import com.quorum.tessera.ServiceLoaderUtil;

public interface VersionInfo {

    default String currentVersion() {
        return VersionInfo.class.getPackage().getSpecificationVersion();
    }

    default String previousVersion() {
        return VersionInfo.class.getPackage().getSpecificationVersion();
    }

    static VersionInfo create() {
        return ServiceLoaderUtil.load(VersionInfo.class)
            .orElseGet(() -> new VersionInfo() {
            });
    }

}
