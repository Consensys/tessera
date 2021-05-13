package com.quorum.tessera.key.vault.aws;

class IncompleteAWSCredentialsException extends IllegalStateException {

  IncompleteAWSCredentialsException(String message) {
    super(message);
  }
}
