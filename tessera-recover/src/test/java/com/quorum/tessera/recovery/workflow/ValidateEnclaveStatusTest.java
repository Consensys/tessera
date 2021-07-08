package com.quorum.tessera.recovery.workflow;

import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveNotAvailableException;
import com.quorum.tessera.service.Service;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ValidateEnclaveStatusTest {

  private ValidateEnclaveStatus validateEnclaveStatus;

  private Enclave enclave;

  @Before
  public void onSetUp() {
    enclave = mock(Enclave.class);
    validateEnclaveStatus = new ValidateEnclaveStatus(enclave);
  }

  @After
  public void onTearDown() {
    verifyNoMoreInteractions(enclave);
  }

  @Test
  public void execute() {
    BatchWorkflowContext context = mock(BatchWorkflowContext.class);

    when(enclave.status()).thenReturn(Service.Status.STARTED);

    validateEnclaveStatus.execute(context);

    verify(enclave).status();
    verifyNoInteractions(context);
  }

  @Test
  public void executeStopped() {
    BatchWorkflowContext context = mock(BatchWorkflowContext.class);

    when(enclave.status()).thenReturn(Service.Status.STOPPED);

    try {
      validateEnclaveStatus.execute(context);
      failBecauseExceptionWasNotThrown(EnclaveNotAvailableException.class);
    } catch (EnclaveNotAvailableException ex) {
      verify(enclave).status();
      verifyNoInteractions(context);
    }
  }
}
