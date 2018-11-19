package com.quorum.tessera.key.vault.azure;

public class AzureCredentialNotSetException extends IllegalStateException {

    public AzureCredentialNotSetException(String message) {
        super(message);
    }

}
