package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveNotAvailableException;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.EncryptorException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.exception.RecipientKeyNotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;


public class SearchRecipentKeyForPayloadTest {

    private SearchRecipentKeyForPayload searchRecipentKeyForPayload;

    private Enclave enclave;

    @Before
    public void onSetUp() {
        enclave = mock(Enclave.class);
        searchRecipentKeyForPayload = new SearchRecipentKeyForPayload(enclave);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(enclave);
    }

    @Test
    public void execute() {


        BatchWorkflowContext encryptedTransactionEvent = new BatchWorkflowContext();
        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(encodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);

        PublicKey publicKey = mock(PublicKey.class);

        encryptedTransactionEvent.setEncodedPayload(encodedPayload);

        when(encodedPayload.getRecipientKeys()).thenReturn(Collections.EMPTY_LIST);
        when(enclave.getPublicKeys()).thenReturn(Set.of(publicKey));

        when(enclave.unencryptTransaction(encodedPayload,publicKey)).thenReturn("OUTCOME".getBytes());

        searchRecipentKeyForPayload.execute(encryptedTransactionEvent);


        assertThat(encryptedTransactionEvent.getEncodedPayload())
            .isExactlyInstanceOf(EncodedPayload.class);

        assertThat(encryptedTransactionEvent.getEncodedPayload().getRecipientKeys())
            .containsExactly(publicKey);


        assertThat(encryptedTransactionEvent.getRecipientKey()).isEqualTo(publicKey);
        assertThat(encryptedTransactionEvent.getRecipient()).isNull();
        assertThat(encryptedTransactionEvent.getEncryptedTransaction()).isNull();

        verify(enclave).unencryptTransaction(encodedPayload,publicKey);
        verify(enclave).getPublicKeys();

    }

    @Test
    public void executeHandleEnclaveExceptions() {

        List<Class<? extends Exception>> handledExceptionTypes = List.of(EnclaveNotAvailableException.class,IndexOutOfBoundsException.class, EncryptorException.class);

        handledExceptionTypes.forEach(t -> {
            BatchWorkflowContext encryptedTransactionEvent = new BatchWorkflowContext();

            EncryptedTransaction encryptedTransaction = mock(EncryptedTransaction.class);
            when(encryptedTransaction.getHash()).thenReturn(mock(MessageHash.class));

            encryptedTransactionEvent.setEncryptedTransaction(encryptedTransaction);

            EncodedPayload encodedPayload = mock(EncodedPayload.class);

            PublicKey publicKey = mock(PublicKey.class);

            encryptedTransactionEvent.setEncodedPayload(encodedPayload);

            when(encodedPayload.getRecipientKeys()).thenReturn(Collections.EMPTY_LIST);
            when(enclave.getPublicKeys()).thenReturn(Set.of(publicKey));

            when(enclave.unencryptTransaction(encodedPayload,publicKey))
                .thenThrow(t);

            try {
                searchRecipentKeyForPayload.execute(encryptedTransactionEvent);
                failBecauseExceptionWasNotThrown(RecipientKeyNotFoundException.class);
            } catch (RecipientKeyNotFoundException ex) {

                assertThat(encryptedTransactionEvent.getEncodedPayload()).isSameAs(encodedPayload);

                assertThat(encryptedTransactionEvent.getRecipientKey()).isNull();
                assertThat(encryptedTransactionEvent.getRecipient()).isNull();
                assertThat(encryptedTransactionEvent.getEncryptedTransaction()).isSameAs(encryptedTransaction);

                verify(enclave).unencryptTransaction(encodedPayload,publicKey);
                verify(enclave).getPublicKeys();

                verifyNoMoreInteractions(enclave);
                reset(enclave);

            }
        });


    }
    @Test
    public void executePayloadContainsKey() {


        BatchWorkflowContext encryptedTransactionEvent = new BatchWorkflowContext();
        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        PublicKey publicKey = mock(PublicKey.class);

        encryptedTransactionEvent.setEncodedPayload(encodedPayload);

        when(encodedPayload.getRecipientKeys()).thenReturn(List.of(publicKey));

        searchRecipentKeyForPayload.execute(encryptedTransactionEvent);

        assertThat(encryptedTransactionEvent.getEncodedPayload())
            .isSameAs(encodedPayload);

        assertThat(encryptedTransactionEvent.getEncodedPayload().getRecipientKeys())
            .containsExactly(publicKey);


        assertThat(encryptedTransactionEvent.getRecipientKey()).isNull();
        assertThat(encryptedTransactionEvent.getRecipient()).isNull();
        assertThat(encryptedTransactionEvent.getEncryptedTransaction()).isNull();


    }
}


