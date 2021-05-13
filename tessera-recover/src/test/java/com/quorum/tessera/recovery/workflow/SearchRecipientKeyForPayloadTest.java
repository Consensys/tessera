package com.quorum.tessera.recovery.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveNotAvailableException;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.EncryptorException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.exception.RecipientKeyNotFoundException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SearchRecipientKeyForPayloadTest {

  private SearchRecipientKeyForPayload searchRecipientKeyForPayload;

  private Enclave enclave;

  @Before
  public void onSetUp() {
    enclave = mock(Enclave.class);

    searchRecipientKeyForPayload = new SearchRecipientKeyForPayload(enclave);
  }

  @After
  public void onTearDown() {
    verifyNoMoreInteractions(enclave);
  }

  @Test
  public void payloadsAlreadyFormatted() {
    final EncodedPayload payloadForRecipient1 =
        EncodedPayload.Builder.create()
            .withRecipientKey(PublicKey.from("recipient1".getBytes()))
            .build();
    final EncodedPayload payloadForRecipient2 =
        EncodedPayload.Builder.create()
            .withRecipientKey(PublicKey.from("recipient2".getBytes()))
            .build();
    final Set<EncodedPayload> preformattedPayloads =
        Set.of(payloadForRecipient1, payloadForRecipient2);

    final BatchWorkflowContext workflowContext = new BatchWorkflowContext();
    workflowContext.setPayloadsToPublish(preformattedPayloads);

    searchRecipientKeyForPayload.execute(workflowContext);

    assertThat(workflowContext.getPayloadsToPublish()).isEqualTo(preformattedPayloads);
  }

  @Test
  public void execute() {
    final BatchWorkflowContext workflowEvent = new BatchWorkflowContext();

    final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
    encryptedTransaction.setHash(new MessageHash("sampleHash".getBytes()));
    workflowEvent.setEncryptedTransaction(encryptedTransaction);

    final EncodedPayload encodedPayloadForRecipient1 =
        EncodedPayload.Builder.create().withRecipientBox("sample-box-1".getBytes()).build();
    final EncodedPayload encodedPayloadForRecipient2 =
        EncodedPayload.Builder.create().withRecipientBox("sample-box-2".getBytes()).build();
    workflowEvent.setPayloadsToPublish(
        Set.of(encodedPayloadForRecipient1, encodedPayloadForRecipient2));

    final PublicKey recipient1 = PublicKey.from("sample-public-key-1".getBytes());
    final PublicKey recipient2 = PublicKey.from("sample-public-key-2".getBytes());

    // Using this LinkedHashSet instead of Set.of(...) to provide a defined iteration order.
    final LinkedHashSet<PublicKey> enclaveKeys = new LinkedHashSet<>();
    enclaveKeys.add(recipient1);
    enclaveKeys.add(recipient2);

    when(enclave.getPublicKeys()).thenReturn(enclaveKeys);
    when(enclave.unencryptTransaction(encodedPayloadForRecipient1, recipient1))
        .thenReturn(new byte[0]);
    when(enclave.unencryptTransaction(encodedPayloadForRecipient2, recipient2))
        .thenReturn(new byte[0]);
    when(enclave.unencryptTransaction(encodedPayloadForRecipient2, recipient1))
        .thenThrow(EncryptorException.class);

    searchRecipientKeyForPayload.execute(workflowEvent);

    final Set<EncodedPayload> updatedPayloads = workflowEvent.getPayloadsToPublish();
    assertThat(updatedPayloads)
        .containsExactlyInAnyOrder(
            EncodedPayload.Builder.from(encodedPayloadForRecipient1)
                .withRecipientKey(recipient1)
                .build(),
            EncodedPayload.Builder.from(encodedPayloadForRecipient2)
                .withRecipientKey(recipient2)
                .build());

    verify(enclave).unencryptTransaction(encodedPayloadForRecipient1, recipient1);
    verify(enclave).unencryptTransaction(encodedPayloadForRecipient2, recipient1);
    verify(enclave).unencryptTransaction(encodedPayloadForRecipient2, recipient2);
    verify(enclave, times(2)).getPublicKeys();

    verifyNoMoreInteractions(enclave);
  }

  @Test
  public void executeHandleEnclaveExceptions() {
    final List<Class<? extends Exception>> handledExceptionTypes =
        List.of(
            EnclaveNotAvailableException.class,
            IndexOutOfBoundsException.class,
            EncryptorException.class);

    handledExceptionTypes.forEach(
        t -> {
          final BatchWorkflowContext workflowEvent = new BatchWorkflowContext();

          final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
          encryptedTransaction.setHash(new MessageHash("sampleHash".getBytes()));
          workflowEvent.setEncryptedTransaction(encryptedTransaction);

          final EncodedPayload encodedPayload = EncodedPayload.Builder.create().build();
          workflowEvent.setPayloadsToPublish(Set.of(encodedPayload));

          final PublicKey publicKey = PublicKey.from("sample-public-key".getBytes());
          when(enclave.getPublicKeys()).thenReturn(Set.of(publicKey));
          when(enclave.unencryptTransaction(encodedPayload, publicKey)).thenThrow(t);

          final Throwable throwable =
              catchThrowable(() -> searchRecipientKeyForPayload.execute(workflowEvent));
          assertThat(throwable)
              .isInstanceOf(RecipientKeyNotFoundException.class)
              .hasMessage("No key found as recipient of message c2FtcGxlSGFzaA==");

          verify(enclave).unencryptTransaction(encodedPayload, publicKey);
          verify(enclave).getPublicKeys();

          verifyNoMoreInteractions(enclave);
          reset(enclave);
        });
  }
}
