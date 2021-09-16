package com.quorum.tessera.transaction.exception;

import com.quorum.tessera.exception.TesseraException;

public class MandatoryRecipientsNotAvailableException extends TesseraException {

  public MandatoryRecipientsNotAvailableException(String message) {
    super(message);
  }
}
