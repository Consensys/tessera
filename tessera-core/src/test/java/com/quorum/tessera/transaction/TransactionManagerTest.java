package com.quorum.tessera.transaction;

import com.quorum.tessera.api.model.ResendRequest;
import com.quorum.tessera.api.model.ResendResponse;
import com.quorum.tessera.api.model.SendRequest;
import com.quorum.tessera.api.model.SendResponse;
import com.quorum.tessera.encryption.KeyManager;
import com.quorum.tessera.nacl.NaclFacade;
import com.quorum.tessera.util.Base64Decoder;
import java.util.Base64;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.mockito.Mockito.*;

@Ignore
public class TransactionManagerTest {

    private TransactionManager transactionManager;

    private PayloadEncoder payloadEncoder;

    private KeyManager keyManager;

    private NaclFacade nacl;

    private EncryptedTransactionDAO encryptedTransactionDAO;

    private PayloadPublisher payloadPublisher;


    @Before
    public void onSetUp() {
        payloadEncoder = mock(PayloadEncoder.class);
        keyManager = mock(KeyManager.class);
        nacl = mock(NaclFacade.class);
        encryptedTransactionDAO = mock(EncryptedTransactionDAO.class);
        payloadPublisher = mock(PayloadPublisher.class);
        
        transactionManager = new TransactionManagerImpl(Base64Decoder.create(), payloadEncoder, keyManager, nacl,
                encryptedTransactionDAO, payloadPublisher);

    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(payloadEncoder, keyManager, nacl, encryptedTransactionDAO, payloadPublisher);
    }

    @Test
    public void send() {


        String sender = Base64.getEncoder().encodeToString("SENDER".getBytes());
        String receiver = Base64.getEncoder().encodeToString("RECEIVER".getBytes());
        
        String payload = Base64.getEncoder().encodeToString("PAYLOAD".getBytes());
        
        SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom(sender);
        sendRequest.setTo(receiver);
        sendRequest.setPayload(payload);
        
        SendResponse result = transactionManager.send(sendRequest);

        assertThat(result).isNotNull();

        
        
    }

    //@Test
    public void resend() {
        ResendRequest resendRequest = new ResendRequest();

        ResendResponse result = transactionManager.resend(resendRequest);
        assertThat(result).isNotNull();
    }

}
