package com.quorum.tessera.transaction.exception;

import com.quorum.tessera.exception.TesseraException;

public class PrivacyViolationException extends TesseraException {

  public PrivacyViolationException(String message) {
    super(message);
  }
}
