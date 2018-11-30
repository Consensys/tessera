package com.quorum.tessera.key.vault.hashicorp;

import com.bettercloud.vault.VaultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FunctionalInterface
public interface VaultCallback<T> {
    Logger LOGGER = LoggerFactory.getLogger(VaultCallback.class);

    T doExecute() throws VaultException;

    static <T> T execute(VaultCallback<T> callback) {
        try {
            return callback.doExecute();
        } catch (final VaultException ex) {
            LOGGER.debug(null, ex);
            throw new HashicorpVaultException(ex);
        }
    }
}
