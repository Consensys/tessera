package com.quorum.tessera.config;

@Deprecated
//Need to sort this out
public enum ConfigHolder {
    INSTANCE;

    private Config config;

    void setConfig(Config config) {
        this.config = config;
    }

    public Config getConfig() {
        return config;
    }

}
