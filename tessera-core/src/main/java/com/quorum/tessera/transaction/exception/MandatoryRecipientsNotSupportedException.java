package com.quorum.tessera.transaction.exception;

import com.quorum.tessera.exception.TesseraException;

public class MandatoryRecipientsNotSupportedException extends TesseraException {

  public MandatoryRecipientsNotSupportedException(String message) {
    super(message);
  }
}
