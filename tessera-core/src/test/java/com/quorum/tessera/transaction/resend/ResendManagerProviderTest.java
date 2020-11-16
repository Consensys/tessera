package com.quorum.tessera.transaction.resend;

import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.enclave.Enclave;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

public class ResendManagerProviderTest {

    @Test
    public void defaultConstructorForCoverage() {
        assertThat(new ResendManagerProvider()).isNotNull();
    }

    @Test
    public void provider() {
        try(var mockedEncryptedTransactionDAO = mockStatic(EncryptedTransactionDAO.class);
            var mockedEnclave = mockStatic(Enclave.class)
        ) {

            Enclave enclave = mock(Enclave.class);

            mockedEnclave.when(Enclave::create).thenReturn(enclave);

            mockedEncryptedTransactionDAO.when(EncryptedTransactionDAO::create)
                .thenReturn(mock(EncryptedTransactionDAO.class));

            ResendManager resendManager = ResendManagerProvider.provider();
            assertThat(resendManager).isNotNull();
        }
    }

}
