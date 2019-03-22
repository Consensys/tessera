package com.quorum.tessera.enclave;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class EnclaveExceptionTest {

    @Test
    public void createWithCause() {
        Throwable cause = new Throwable();
        EnclaveException enclaveException = new EnclaveException(cause);

        assertThat(enclaveException.getCause()).isSameAs(cause);
    }

    @Test
    public void createWithCauseAndMessage() {
        Throwable cause = new Throwable();
        EnclaveException enclaveException = new EnclaveException("Ouch", cause);

        assertThat(enclaveException.getCause()).isSameAs(cause);
        assertThat(enclaveException.getMessage()).isEqualTo("Ouch");

    }

    @Test
    public void createWithMessage() {
        EnclaveException enclaveException = new EnclaveException("Ouch");
        assertThat(enclaveException.getCause()).isNull();
        assertThat(enclaveException.getMessage()).isEqualTo("Ouch");

    }

}
