package com.quorum.tessera.enclave.rest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.quorum.tessera.enclave.EnclaveNotAvailableException;
import jakarta.ws.rs.ProcessingException;
import java.net.ConnectException;
import org.junit.Test;

public class ClientCallbackTest {

  @Test(expected = EnclaveNotAvailableException.class)
  public void handleConnectivityIssue() {

    ClientCallback callback = mock(ClientCallback.class);
    when(callback.doExecute()).thenThrow(new ProcessingException(new ConnectException("OUCH")));

    ClientCallback.execute(callback);
  }

  @Test(expected = ProcessingException.class)
  public void handleNonConnectivityIssue() {

    ClientCallback callback = mock(ClientCallback.class);
    when(callback.doExecute()).thenThrow(new ProcessingException(new Exception("OUCH")));

    ClientCallback.execute(callback);
  }
}
