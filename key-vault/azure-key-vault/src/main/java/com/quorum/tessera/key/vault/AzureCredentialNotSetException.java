package com.quorum.tessera.key.vault;

public class AzureCredentialNotSetException extends IllegalStateException {

    public AzureCredentialNotSetException(String message) {
        super(message);
    }

}
