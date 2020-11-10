package com.quorum.tessera.discovery;

public class NetworkStoreFactory {

    public static NetworkStore provider() {
        return DefaultNetworkStore.INSTANCE;
    }


}
