package com.quorum.tessera.key.vault.hashicorp;

public class HashicorpCredentialNotSetException extends IllegalStateException {

    public HashicorpCredentialNotSetException(String message) {
        super(message);
    }

}
