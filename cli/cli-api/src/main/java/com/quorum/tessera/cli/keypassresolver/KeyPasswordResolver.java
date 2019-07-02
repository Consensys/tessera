package com.quorum.tessera.cli.keypassresolver;

import com.quorum.tessera.config.Config;

public interface KeyPasswordResolver {

    /**
     * Attempts to resolve all the locked keys using/fetching the required password.
     *
     * <p>The passwords may be provided in the configuration, or they may be fetched from other sources as required.
     *
     * @param config the configuration that contains the keys to resolve, as well as some predefined passwords
     */
    void resolveKeyPasswords(Config config);
}
