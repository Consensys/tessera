package com.quorum.tessera.key.vault.aws;

import com.quorum.tessera.key.vault.KeyVaultException;

class AWSSecretsManagerException extends KeyVaultException {
  AWSSecretsManagerException(Throwable cause) {
    super(cause);
  }
}
