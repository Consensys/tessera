package com.quorum.tessera.enclave;

import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class EnclaveHolderTest {

    @Ignore
    @Test
    public void getInstance() {
        assertThat(EnclaveHolder.getInstance())
            .isNotNull().isExactlyInstanceOf(MockEnclaveHolder.class);
    }

    @Test
    public void setAndGetDefaultEnclaveHolder() {
        DefaultEnclaveHolder.INSTANCE.reset();
        EnclaveHolder enclaveHolder = DefaultEnclaveHolder.INSTANCE;
        assertThat(enclaveHolder.getEnclave()).isEmpty();

        Enclave enclave = mock(Enclave.class);
        Enclave result = enclaveHolder.setEnclave(enclave);
        assertThat(result).isSameAs(enclave);

        assertThat(enclaveHolder.getEnclave()).isPresent().containsSame(enclave);

    }

    @Test(expected = IllegalArgumentException.class)
    public void thereCanBeOnlyOneStoreEnclae() {
        DefaultEnclaveHolder.INSTANCE.reset();
        EnclaveHolder enclaveHolder = DefaultEnclaveHolder.INSTANCE;
        enclaveHolder.setEnclave(mock(Enclave.class));
        enclaveHolder.setEnclave(mock(Enclave.class));


    }

}
