package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.enclave.EnclaveNotAvailableException;
import java.net.ConnectException;
import javax.ws.rs.ProcessingException;

public interface ClientCallback<T> {

  T doExecute() throws ProcessingException;

  static <T> T execute(ClientCallback<T> callback) {
    try {
      return callback.doExecute();
    } catch (ProcessingException ex) {
      if (ConnectException.class.isInstance(ex.getCause())) {
        throw new EnclaveNotAvailableException(ex.getCause().getMessage());
      }
      throw ex;
    }
  }
}
