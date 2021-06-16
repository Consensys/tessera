package com.quorum.tessera.base64;

import com.quorum.tessera.exception.TesseraException;

/** An exception thrown if an input is not valid Base64 and cannot be decoded */
public class DecodingException extends TesseraException {

  public DecodingException(final Throwable cause) {
    super(cause);
  }
}
