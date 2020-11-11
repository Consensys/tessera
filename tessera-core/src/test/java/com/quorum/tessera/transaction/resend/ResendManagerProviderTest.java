package com.quorum.tessera.transaction.resend;

import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ResendManagerProviderTest {

    @Test
    public void defaultConstructorForCoverage() {
        assertThat(new ResendManagerProvider()).isNotNull();
    }

    @Test
    public void provider() {
        try(var mockedEncryptedTransactionDAO = mockStatic(EncryptedTransactionDAO.class);
            var mockedEnclaveFactory = mockStatic(EnclaveFactory.class)
        ) {

            EnclaveFactory enclaveFactory = mock(EnclaveFactory.class);
            when(enclaveFactory.enclave()).thenReturn(Optional.of(mock(Enclave.class)));

            mockedEnclaveFactory.when(EnclaveFactory::create).thenReturn(enclaveFactory);

            mockedEncryptedTransactionDAO.when(EncryptedTransactionDAO::create)
                .thenReturn(mock(EncryptedTransactionDAO.class));

            ResendManager resendManager = ResendManagerProvider.provider();
            assertThat(resendManager).isNotNull();
        }
    }

}
