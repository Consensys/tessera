package com.quorum.tessera.api;

import com.quorum.tessera.version.ApiVersion;

import java.util.Comparator;

public class Version {

    public static String getVersion() {
        return ApiVersion.versions()
            .stream()
            .sorted(Comparator.comparing(String::toString).reversed())
            .findFirst()
            .get();
    }
}
