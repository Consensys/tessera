package com.quorum.tessera.discovery;

import java.util.ServiceLoader;

public interface OnCreateHelper {

    void onCreate();

    static OnCreateHelper getInstance() {
        return ServiceLoader.load(OnCreateHelper.class).findFirst().get();
    }
}
