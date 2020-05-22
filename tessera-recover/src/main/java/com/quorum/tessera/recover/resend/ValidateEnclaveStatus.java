package com.quorum.tessera.recover.resend;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveNotAvailableException;
import com.quorum.tessera.service.Service;

public class ValidateEnclaveStatus implements BatchWorkflowAction {

    private Enclave enclave;

    public ValidateEnclaveStatus(Enclave enclave) {
        this.enclave = enclave;
    }

    @Override
    public boolean execute(BatchWorkflowContext context) {
        if (enclave.status() == Service.Status.STOPPED) {
            throw new EnclaveNotAvailableException();
        }
        return true;
    }
}
