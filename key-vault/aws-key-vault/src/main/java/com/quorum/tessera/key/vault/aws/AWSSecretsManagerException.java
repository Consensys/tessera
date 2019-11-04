package com.quorum.tessera.key.vault.aws;

import com.quorum.tessera.key.vault.KeyVaultException;

public class AWSSecretsManagerException extends KeyVaultException {
    public AWSSecretsManagerException(Throwable cause) {
        super(cause);
    }
}
