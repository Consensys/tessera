package com.quorum.tessera.core.api;

import com.jpmorgan.quorum.mock.servicelocator.MockServiceLocator;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.service.locator.ServiceLocator;
import com.quorum.tessera.data.EncryptedRawTransactionDAO;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.partyinfo.PayloadPublisher;
import com.quorum.tessera.partyinfo.ResendManager;
import com.quorum.tessera.transaction.TransactionManager;
import java.util.HashSet;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;

public class ServiceFactoryTest {

    private MockServiceLocator mockServiceLocator;

    private ServiceFactoryImpl serviceFactory;

    @Before
    public void onSetUp() throws Exception {
        mockServiceLocator = (MockServiceLocator) ServiceLocator.create();
        Set services = new HashSet();
        services.add(mock(Config.class));
        services.add(mock(Enclave.class));
        services.add(mock(TransactionManager.class));
        services.add(mock(PartyInfoService.class));
        services.add(mock(EncryptedTransactionDAO.class));
        services.add(mock(EncryptedRawTransactionDAO.class));
        services.add(mock(ResendManager.class));
        services.add(mock(PayloadPublisher.class));

        mockServiceLocator.setServices(services);

        serviceFactory = (ServiceFactoryImpl) ServiceFactory.create();
    }

    @Test
    public void enclave() {
        Enclave enclave = serviceFactory.enclave();
        assertThat(enclave).isNotNull();
    }

    @Test
    public void transactionManager() {
        TransactionManager transactionManager = serviceFactory.transactionManager();
        assertThat(transactionManager).isNotNull();
    }

    @Test
    public void partyInfoService() {
        PartyInfoService partyInfoService = serviceFactory.partyInfoService();
        assertThat(partyInfoService).isNotNull();
    }

    @Test
    public void encryptedTransactionDAO() {
        EncryptedTransactionDAO encryptedTransactionDAO = serviceFactory.encryptedTransactionDAO();
        assertThat(encryptedTransactionDAO).isNotNull();
    }

    @Test(expected = IllegalStateException.class)
    public void findNoServiceFoundThrowsIllegalState() {

        serviceFactory.find(NonExistentService.class);
    }

    static class NonExistentService {}

    @Test
    public void findEncryptedRawTransactionDAO() {
        EncryptedRawTransactionDAO encryptedRawTransactionDAO = serviceFactory.encryptedRawTransactionDAO();
        assertThat(encryptedRawTransactionDAO).isNotNull();
    }

    @Test
    public void findResendManager() {
        ResendManager resendManager = serviceFactory.resendManager();
        assertThat(resendManager).isNotNull();
    }

    @Test
    public void findPayloadPublisher() {
        PayloadPublisher payloadPublisher = serviceFactory.payloadPublisher();
        assertThat(payloadPublisher).isNotNull();
    }

}
