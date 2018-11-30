package com.quorum.tessera.key.vault.hashicorp;

import com.quorum.tessera.key.vault.KeyVaultException;

public class HashicorpVaultException extends KeyVaultException {

    public HashicorpVaultException(Throwable cause) {
        super(cause);
    }

    public HashicorpVaultException(String message) {
        super(message);
    }
}
