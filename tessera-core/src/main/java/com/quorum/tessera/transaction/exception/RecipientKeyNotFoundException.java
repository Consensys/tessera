package com.quorum.tessera.transaction.exception;

import com.quorum.tessera.exception.TesseraException;

public class RecipientKeyNotFoundException extends TesseraException {

  public RecipientKeyNotFoundException(String message) {
    super(message);
  }
}
