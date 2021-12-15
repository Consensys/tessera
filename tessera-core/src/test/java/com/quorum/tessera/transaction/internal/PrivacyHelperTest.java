package com.quorum.tessera.transaction.internal;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.PrivacyHelper;
import com.quorum.tessera.transaction.exception.EnhancedPrivacyNotSupportedException;
import com.quorum.tessera.transaction.exception.PrivacyViolationException;
import java.util.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PrivacyHelperTest {

  private PrivacyHelper privacyHelper;

  private EncryptedTransactionDAO encryptedTransactionDAO;

  @Before
  public void setUp() {
    encryptedTransactionDAO = mock(EncryptedTransactionDAO.class);
    privacyHelper = new PrivacyHelperImpl(encryptedTransactionDAO, true);
  }

  @After
  public void onTearDown() {
    verifyNoMoreInteractions(encryptedTransactionDAO);
  }

  @Test
  public void create() {
    try (var mockedServiceLoader = mockStatic(ServiceLoader.class)) {
      PrivacyHelper privacyHelper = mock(PrivacyHelper.class);
      ServiceLoader serviceLoader = mock(ServiceLoader.class);
      when(serviceLoader.findFirst()).thenReturn(Optional.of(privacyHelper));
      mockedServiceLoader
          .when(() -> ServiceLoader.load(PrivacyHelper.class))
          .thenReturn(serviceLoader);
      PrivacyHelper.create();
      mockedServiceLoader.verify(() -> ServiceLoader.load(PrivacyHelper.class));
      verify(serviceLoader).findFirst();

      mockedServiceLoader.verifyNoMoreInteractions();
      verifyNoMoreInteractions(serviceLoader);
      verifyNoInteractions(privacyHelper);
    }
  }

  @Test
  public void findAffectedContractTransactionsFromSendRequestFound() {

    final MessageHash hash1 = mock(MessageHash.class);
    final MessageHash hash2 = mock(MessageHash.class);

    EncryptedTransaction et1 = mock(EncryptedTransaction.class);
    when(et1.getEncodedPayload()).thenReturn("payload1".getBytes());
    when(et1.getHash()).thenReturn(hash1);
    when(et1.getPayload()).thenReturn(mock(EncodedPayload.class));

    EncryptedTransaction et2 = mock(EncryptedTransaction.class);
    when(et2.getEncodedPayload()).thenReturn("payload2".getBytes());
    when(et2.getHash()).thenReturn(hash2);
    when(et2.getPayload()).thenReturn(mock(EncodedPayload.class));

    when(encryptedTransactionDAO.findByHashes(anyCollection())).thenReturn(List.of(et1, et2));

    List<AffectedTransaction> affectedTransactions =
        privacyHelper.findAffectedContractTransactionsFromSendRequest(Set.of(hash1, hash2));

    assertThat(affectedTransactions).isNotNull();
    assertThat(affectedTransactions.size()).isEqualTo(2);

    verify(encryptedTransactionDAO).findByHashes(any());
  }

  @Test
  public void findAffectedContractTransactionsFromSendRequestNotFound() {

    final MessageHash hash1 = mock(MessageHash.class);
    final MessageHash hash2 = mock(MessageHash.class);

    EncryptedTransaction et1 = mock(EncryptedTransaction.class);
    when(et1.getEncodedPayload()).thenReturn("payload1".getBytes());
    when(et1.getHash()).thenReturn(new MessageHash("hash1".getBytes()));

    when(encryptedTransactionDAO.findByHashes(anyCollection())).thenReturn(List.of(et1));

    assertThatExceptionOfType(PrivacyViolationException.class)
        .isThrownBy(
            () -> {
              privacyHelper.findAffectedContractTransactionsFromSendRequest(Set.of(hash1, hash2));
              failBecauseExceptionWasNotThrown(Exception.class);
            })
        .withMessageContaining("Unable to find affectedContractTransaction");

    verify(encryptedTransactionDAO).findByHashes(any());
  }

  @Test
  public void testValidateSendPartyProtection() {

    final EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(encodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.PARTY_PROTECTION);

    final AffectedTransaction affectedTransaction = mock(AffectedTransaction.class);
    when(affectedTransaction.getPayload()).thenReturn(encodedPayload);

    boolean isValid =
        privacyHelper.validateSendRequest(
            PrivacyMode.PARTY_PROTECTION,
            Collections.emptyList(),
            singletonList(affectedTransaction),
            emptySet());

    assertThat(isValid).isTrue();
  }

  @Test
  public void testValidateSendPartyProtectionFlagMismatched() {

    final EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(encodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.PARTY_PROTECTION);

    final AffectedTransaction affectedTransaction = mock(AffectedTransaction.class);
    when(affectedTransaction.getPayload()).thenReturn(encodedPayload);
    final TxHash hash = TxHash.from("someHash".getBytes());
    when(affectedTransaction.getHash()).thenReturn(hash);

    assertThatExceptionOfType(PrivacyViolationException.class)
        .isThrownBy(
            () ->
                privacyHelper.validateSendRequest(
                    PrivacyMode.PRIVATE_STATE_VALIDATION,
                    Collections.emptyList(),
                    singletonList(affectedTransaction),
                    emptySet()))
        .withMessage("Privacy metadata mismatched with Affected Txn " + hash.encodeToBase64());
  }

  @Test
  public void testValidateSendPsv() {

    PublicKey recipient1 = mock(PublicKey.class);
    PublicKey recipient2 = mock(PublicKey.class);

    final EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(encodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
    when(encodedPayload.getRecipientKeys()).thenReturn(List.of(recipient1, recipient2));

    final AffectedTransaction affectedTransaction = mock(AffectedTransaction.class);
    when(affectedTransaction.getPayload()).thenReturn(encodedPayload);
    final TxHash hash = TxHash.from("someHash".getBytes());
    when(affectedTransaction.getHash()).thenReturn(hash);

    boolean isValid =
        privacyHelper.validateSendRequest(
            PrivacyMode.PRIVATE_STATE_VALIDATION,
            List.of(recipient1, recipient2),
            singletonList(affectedTransaction),
            emptySet());

    assertThat(isValid).isTrue();
  }

  @Test
  public void testValidateSendPsvMoreRecipientsAffected() {

    PublicKey recipient1 = mock(PublicKey.class);
    PublicKey recipient2 = mock(PublicKey.class);

    final EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(encodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
    when(encodedPayload.getRecipientKeys()).thenReturn(List.of(recipient1, recipient2));

    final AffectedTransaction affectedTransaction = mock(AffectedTransaction.class);
    when(affectedTransaction.getPayload()).thenReturn(encodedPayload);
    final TxHash hash = TxHash.from("someHash".getBytes());
    when(affectedTransaction.getHash()).thenReturn(hash);

    assertThatExceptionOfType(PrivacyViolationException.class)
        .isThrownBy(
            () ->
                privacyHelper.validateSendRequest(
                    PrivacyMode.PRIVATE_STATE_VALIDATION,
                    List.of(recipient1),
                    singletonList(affectedTransaction),
                    emptySet()))
        .withMessage("Recipients mismatched for Affected Txn " + hash.encodeToBase64());
  }

  @Test
  public void testValidateSendPsvLessRecipientsAffected() {

    PublicKey recipient1 = mock(PublicKey.class);
    PublicKey recipient2 = mock(PublicKey.class);

    final EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(encodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
    when(encodedPayload.getRecipientKeys()).thenReturn(List.of(recipient1));

    final AffectedTransaction affectedTransaction = mock(AffectedTransaction.class);
    when(affectedTransaction.getPayload()).thenReturn(encodedPayload);
    final TxHash hash = TxHash.from("someHash".getBytes());
    when(affectedTransaction.getHash()).thenReturn(hash);

    assertThatExceptionOfType(PrivacyViolationException.class)
        .isThrownBy(
            () ->
                privacyHelper.validateSendRequest(
                    PrivacyMode.PRIVATE_STATE_VALIDATION,
                    List.of(recipient1, recipient2),
                    singletonList(affectedTransaction),
                    emptySet()))
        .withMessage("Recipients mismatched for Affected Txn " + hash.encodeToBase64());
  }

  @Test
  public void findAffectedContractTransactionsFromPayload() {

    final EncodedPayload payload = mock(EncodedPayload.class);
    Map<TxHash, SecurityHash> affected = new HashMap<>();
    affected.put(TxHash.from("Hash1".getBytes()), SecurityHash.from("secHash1".getBytes()));
    affected.put(TxHash.from("Hash2".getBytes()), SecurityHash.from("secHash2".getBytes()));

    EncryptedTransaction et1 = mock(EncryptedTransaction.class);
    when(et1.getEncodedPayload()).thenReturn("payload1".getBytes());
    when(et1.getHash()).thenReturn(new MessageHash("Hash1".getBytes()));
    when(et1.getPayload()).thenReturn(mock(EncodedPayload.class));

    when(payload.getAffectedContractTransactions()).thenReturn(affected);
    when(encryptedTransactionDAO.findByHashes(any())).thenReturn(singletonList(et1));

    List<AffectedTransaction> result =
        privacyHelper.findAffectedContractTransactionsFromPayload(payload);

    assertThat(result).hasSize(1);

    verify(encryptedTransactionDAO).findByHashes(any());
  }

  @Test
  public void validatePayloadFlagMismatched() {

    TxHash txHash = mock(TxHash.class);
    EncodedPayload payload = mock(EncodedPayload.class);
    when(payload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);

    EncodedPayload affectedPayload1 = mock(EncodedPayload.class);
    when(affectedPayload1.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    AffectedTransaction affectedTransaction1 = mock(AffectedTransaction.class);
    when(affectedTransaction1.getPayload()).thenReturn(affectedPayload1);

    EncodedPayload affectedPayload2 = mock(EncodedPayload.class);
    when(affectedPayload2.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
    AffectedTransaction affectedTransaction2 = mock(AffectedTransaction.class);
    when(affectedTransaction2.getPayload()).thenReturn(affectedPayload2);

    boolean result =
        privacyHelper.validatePayload(
            txHash, payload, List.of(affectedTransaction1, affectedTransaction2));

    assertThat(result).isFalse();
  }

  @Test
  public void validatePayloadMandatoryRecipientsMismatched() {

    TxHash txHash = mock(TxHash.class);
    EncodedPayload payload = mock(EncodedPayload.class);
    when(payload.getPrivacyMode()).thenReturn(PrivacyMode.MANDATORY_RECIPIENTS);
    PublicKey mandatory1 = mock(PublicKey.class);
    PublicKey mandatory2 = mock(PublicKey.class);
    when(payload.getMandatoryRecipients()).thenReturn(Set.of(mandatory1));

    EncodedPayload affectedPayload1 = mock(EncodedPayload.class);
    when(affectedPayload1.getPrivacyMode()).thenReturn(PrivacyMode.MANDATORY_RECIPIENTS);
    when(affectedPayload1.getMandatoryRecipients()).thenReturn(Set.of(mandatory1, mandatory2));
    AffectedTransaction affectedTransaction1 = mock(AffectedTransaction.class);
    when(affectedTransaction1.getPayload()).thenReturn(affectedPayload1);

    EncodedPayload affectedPayload2 = mock(EncodedPayload.class);
    when(affectedPayload2.getPrivacyMode()).thenReturn(PrivacyMode.MANDATORY_RECIPIENTS);
    when(affectedPayload2.getMandatoryRecipients()).thenReturn(Set.of(mandatory1, mandatory2));
    AffectedTransaction affectedTransaction2 = mock(AffectedTransaction.class);
    when(affectedTransaction2.getPayload()).thenReturn(affectedPayload2);

    boolean result =
        privacyHelper.validatePayload(
            txHash, payload, List.of(affectedTransaction1, affectedTransaction2));

    assertThat(result).isFalse();
  }

  @Test
  public void validPayloadMandatoryRecipients() {

    TxHash txHash = mock(TxHash.class);
    EncodedPayload payload = mock(EncodedPayload.class);
    when(payload.getPrivacyMode()).thenReturn(PrivacyMode.MANDATORY_RECIPIENTS);
    PublicKey mandatory1 = mock(PublicKey.class);
    PublicKey mandatory2 = mock(PublicKey.class);
    when(payload.getMandatoryRecipients()).thenReturn(Set.of(mandatory1, mandatory2));

    EncodedPayload affectedPayload1 = mock(EncodedPayload.class);
    when(affectedPayload1.getPrivacyMode()).thenReturn(PrivacyMode.MANDATORY_RECIPIENTS);
    when(affectedPayload1.getMandatoryRecipients()).thenReturn(Set.of(mandatory1));
    AffectedTransaction affectedTransaction1 = mock(AffectedTransaction.class);
    when(affectedTransaction1.getPayload()).thenReturn(affectedPayload1);

    EncodedPayload affectedPayload2 = mock(EncodedPayload.class);
    when(affectedPayload2.getPrivacyMode()).thenReturn(PrivacyMode.MANDATORY_RECIPIENTS);
    when(affectedPayload2.getMandatoryRecipients()).thenReturn(Set.of(mandatory2));
    AffectedTransaction affectedTransaction2 = mock(AffectedTransaction.class);
    when(affectedTransaction2.getPayload()).thenReturn(affectedPayload2);

    boolean result =
        privacyHelper.validatePayload(
            txHash, payload, List.of(affectedTransaction1, affectedTransaction2));

    assertThat(result).isTrue();
  }

  @Test
  public void validatePsvPayloadWithMissingAffectedTxs() {
    final TxHash txHash = mock(TxHash.class);
    EncodedPayload payload = mock(EncodedPayload.class);
    when(payload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);

    Map<TxHash, SecurityHash> affected = new HashMap<>();
    affected.put(TxHash.from("Hash1".getBytes()), SecurityHash.from("secHash1".getBytes()));
    affected.put(TxHash.from("Hash2".getBytes()), SecurityHash.from("secHash2".getBytes()));

    when(payload.getAffectedContractTransactions()).thenReturn(affected);

    EncodedPayload affectedPayload = mock(EncodedPayload.class);
    when(affectedPayload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
    AffectedTransaction affectedTransaction = mock(AffectedTransaction.class);
    when(affectedTransaction.getPayload()).thenReturn(affectedPayload);

    boolean result =
        privacyHelper.validatePayload(txHash, payload, singletonList(affectedTransaction));

    assertThat(result).isFalse();
  }

  @Test
  public void validatePayloadPsvFakeSender() {

    final PublicKey recipient1 = PublicKey.from("Recipient1".getBytes());
    final PublicKey recipient2 = PublicKey.from("Recipient2".getBytes());
    final PublicKey fakeSender = PublicKey.from("someone".getBytes());
    final TxHash txHash = TxHash.from("someHash".getBytes());
    EncodedPayload payload = mock(EncodedPayload.class);
    when(payload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
    when(payload.getSenderKey()).thenReturn(fakeSender);

    Map<TxHash, SecurityHash> affected = new HashMap<>();
    affected.put(TxHash.from("Hash1".getBytes()), SecurityHash.from("secHash1".getBytes()));
    affected.put(TxHash.from("Hash2".getBytes()), SecurityHash.from("secHash2".getBytes()));

    when(payload.getAffectedContractTransactions()).thenReturn(affected);

    EncodedPayload affectedPayload1 = mock(EncodedPayload.class);
    when(affectedPayload1.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
    when(affectedPayload1.getRecipientKeys()).thenReturn(List.of(recipient1, fakeSender));
    AffectedTransaction affectedTransaction1 = mock(AffectedTransaction.class);
    when(affectedTransaction1.getPayload()).thenReturn(affectedPayload1);
    when(affectedTransaction1.getHash()).thenReturn(TxHash.from("hash1".getBytes()));

    EncodedPayload affectedPayload2 = mock(EncodedPayload.class);
    when(affectedPayload2.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
    when(affectedPayload2.getRecipientKeys()).thenReturn(List.of(recipient1, recipient2));
    AffectedTransaction affectedTransaction2 = mock(AffectedTransaction.class);
    when(affectedTransaction2.getPayload()).thenReturn(affectedPayload2);
    when(affectedTransaction2.getHash()).thenReturn(TxHash.from("hash2".getBytes()));

    boolean result =
        privacyHelper.validatePayload(
            txHash, payload, List.of(affectedTransaction1, affectedTransaction2));

    assertThat(result).isFalse();
  }

  @Test
  public void validatePsvPayloadRecipientsMismatched() {

    PublicKey recipient1 = PublicKey.from("Recipient1".getBytes());
    PublicKey recipient2 = PublicKey.from("Recipient2".getBytes());
    final TxHash txHash = TxHash.from("someHash".getBytes());
    EncodedPayload payload = mock(EncodedPayload.class);
    when(payload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
    when(payload.getSenderKey()).thenReturn(recipient1);
    when(payload.getRecipientKeys()).thenReturn(List.of(recipient1, recipient2));

    Map<TxHash, SecurityHash> affected = new HashMap<>();
    affected.put(TxHash.from("Hash1".getBytes()), SecurityHash.from("secHash1".getBytes()));
    affected.put(TxHash.from("Hash2".getBytes()), SecurityHash.from("secHash2".getBytes()));

    when(payload.getAffectedContractTransactions()).thenReturn(affected);

    EncodedPayload affectedPayload1 = mock(EncodedPayload.class);
    when(affectedPayload1.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
    when(affectedPayload1.getRecipientKeys()).thenReturn(singletonList(recipient1));
    AffectedTransaction affectedTransaction1 = mock(AffectedTransaction.class);
    when(affectedTransaction1.getPayload()).thenReturn(affectedPayload1);
    when(affectedTransaction1.getHash()).thenReturn(TxHash.from("hash1".getBytes()));

    EncodedPayload affectedPayload2 = mock(EncodedPayload.class);
    when(affectedPayload2.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
    when(affectedPayload2.getRecipientKeys()).thenReturn(List.of(recipient1, recipient2));
    AffectedTransaction affectedTransaction2 = mock(AffectedTransaction.class);
    when(affectedTransaction2.getPayload()).thenReturn(affectedPayload2);
    when(affectedTransaction2.getHash()).thenReturn(TxHash.from("hash2".getBytes()));

    assertThatExceptionOfType(PrivacyViolationException.class)
        .isThrownBy(
            () ->
                privacyHelper.validatePayload(
                    txHash, payload, List.of(affectedTransaction1, affectedTransaction2)))
        .withMessage(
            "Recipients mismatched for Affected Txn "
                + TxHash.from("hash1".getBytes()).encodeToBase64());
  }

  @Test
  public void validPayload() {

    PublicKey recipient1 = PublicKey.from("Recipient1".getBytes());
    PublicKey recipient2 = PublicKey.from("Recipient2".getBytes());
    final TxHash txHash = TxHash.from("someHash".getBytes());
    EncodedPayload payload = mock(EncodedPayload.class);
    when(payload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
    when(payload.getSenderKey()).thenReturn(recipient1);
    when(payload.getRecipientKeys()).thenReturn(List.of(recipient1, recipient2));

    Map<TxHash, SecurityHash> affected = new HashMap<>();
    affected.put(TxHash.from("Hash1".getBytes()), SecurityHash.from("secHash1".getBytes()));
    affected.put(TxHash.from("Hash2".getBytes()), SecurityHash.from("secHash2".getBytes()));

    when(payload.getAffectedContractTransactions()).thenReturn(affected);

    EncodedPayload affectedPayload1 = mock(EncodedPayload.class);
    when(affectedPayload1.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
    when(affectedPayload1.getRecipientKeys()).thenReturn(List.of(recipient1, recipient2));
    AffectedTransaction affectedTransaction1 = mock(AffectedTransaction.class);
    when(affectedTransaction1.getPayload()).thenReturn(affectedPayload1);
    when(affectedTransaction1.getHash()).thenReturn(TxHash.from("hash1".getBytes()));

    EncodedPayload affectedPayload2 = mock(EncodedPayload.class);
    when(affectedPayload2.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
    when(affectedPayload2.getRecipientKeys()).thenReturn(List.of(recipient2, recipient1));
    AffectedTransaction affectedTransaction2 = mock(AffectedTransaction.class);
    when(affectedTransaction2.getPayload()).thenReturn(affectedPayload2);
    when(affectedTransaction2.getHash()).thenReturn(TxHash.from("hash2".getBytes()));

    boolean result =
        privacyHelper.validatePayload(
            txHash, payload, List.of(affectedTransaction1, affectedTransaction2));

    assertThat(result).isTrue();
  }

  @Test
  public void psvTransactionCannotHaveInvalidHashes() {

    TxHash txHash = TxHash.from("Hash1".getBytes());
    TxHash invalid = TxHash.from("InvalidHash".getBytes());
    EncodedPayload payload = mock(EncodedPayload.class);
    when(payload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
    Set<TxHash> invalidHashes = Set.of(invalid);

    assertThatExceptionOfType(PrivacyViolationException.class)
        .isThrownBy(() -> privacyHelper.sanitisePrivacyPayload(txHash, payload, invalidHashes))
        .withMessage(
            "Invalid security hashes identified for PSC TX "
                + txHash
                + ". Invalid ACOTHs: "
                + invalid.encodeToBase64());
  }

  @Test
  public void sanitisedInputForPartyProtection() {
    final TxHash txHash = TxHash.from("Hash1".getBytes());
    TxHash invalid = TxHash.from("InvalidHash".getBytes());

    Map<TxHash, byte[]> affected = new HashMap<>();
    affected.put(TxHash.from("Hash1".getBytes()), "secHash1".getBytes());
    affected.put(invalid, "secHash2".getBytes());

    EncodedPayload payload =
        EncodedPayload.Builder.create()
            .withPrivacyMode(PrivacyMode.PARTY_PROTECTION)
            .withAffectedContractTransactions(affected)
            .build();

    assertThat(payload.getAffectedContractTransactions()).hasSize(2);

    Set<TxHash> invalidHashes = Set.of(invalid);

    final EncodedPayload updatedPayload =
        privacyHelper.sanitisePrivacyPayload(txHash, payload, invalidHashes);

    assertThat(updatedPayload.getAffectedContractTransactions()).hasSize(1);
  }

  @Test
  public void returnsEmptyList() {
    assertThat(privacyHelper.findAffectedContractTransactionsFromSendRequest(null)).hasSize(0);
    assertThat(privacyHelper.findAffectedContractTransactionsFromSendRequest(Collections.EMPTY_SET))
        .hasSize(0);

    EncodedPayload payload = mock(EncodedPayload.class);
    when(payload.getAffectedContractTransactions()).thenReturn(emptyMap());
    assertThat(privacyHelper.findAffectedContractTransactionsFromPayload(payload)).hasSize(0);
  }

  @Test
  public void throwExceptionForSendRequestWhenPrivacyNotEnabled() {
    final PrivacyHelper anotherHelper = new PrivacyHelperImpl(encryptedTransactionDAO, false);

    assertThatExceptionOfType(EnhancedPrivacyNotSupportedException.class)
        .isThrownBy(
            () ->
                anotherHelper.validateSendRequest(
                    PrivacyMode.PRIVATE_STATE_VALIDATION, emptyList(), emptyList(), emptySet()));
  }

  @Test
  public void throwExceptionForPayloadWhenPrivacyNotEnabled() {
    final PrivacyHelper anotherHelper = new PrivacyHelperImpl(encryptedTransactionDAO, false);

    EncodedPayload payload = mock(EncodedPayload.class);
    when(payload.getPrivacyMode()).thenReturn(PrivacyMode.PARTY_PROTECTION);

    assertThatExceptionOfType(EnhancedPrivacyNotSupportedException.class)
        .isThrownBy(() -> anotherHelper.validatePayload(mock(TxHash.class), payload, emptyList()));
  }

  @Test
  public void testValidateSendMandatoryRecipientsInvalid() {

    PublicKey recipient1 = mock(PublicKey.class);
    PublicKey recipient2 = mock(PublicKey.class);

    assertThatExceptionOfType(PrivacyViolationException.class)
        .isThrownBy(
            () ->
                privacyHelper.validateSendRequest(
                    PrivacyMode.MANDATORY_RECIPIENTS,
                    List.of(recipient1, recipient2),
                    emptyList(),
                    Set.of(mock(PublicKey.class))))
        .withMessageContaining(
            "One or more mandatory recipients not included in the participant list");
  }

  @Test
  public void testValidateSendMandatoryRecipientsMismatched() {

    PublicKey recipient1 = mock(PublicKey.class);
    PublicKey recipient2 = mock(PublicKey.class);

    final EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(encodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.MANDATORY_RECIPIENTS);
    when(encodedPayload.getRecipientKeys()).thenReturn(List.of(recipient1, recipient2));
    when(encodedPayload.getMandatoryRecipients()).thenReturn(Set.of(mock(PublicKey.class)));

    final AffectedTransaction affectedTransaction = mock(AffectedTransaction.class);
    when(affectedTransaction.getPayload()).thenReturn(encodedPayload);
    final TxHash hash = TxHash.from("someHash".getBytes());
    when(affectedTransaction.getHash()).thenReturn(hash);

    assertThatExceptionOfType(PrivacyViolationException.class)
        .isThrownBy(
            () ->
                privacyHelper.validateSendRequest(
                    PrivacyMode.MANDATORY_RECIPIENTS,
                    List.of(recipient1, recipient2),
                    singletonList(affectedTransaction),
                    Set.of(recipient1)))
        .withMessageContaining("Privacy metadata mismatched");
  }

  @Test
  public void testValidSendMandatoryRecipients() {

    PublicKey recipient1 = mock(PublicKey.class);
    PublicKey recipient2 = mock(PublicKey.class);

    final EncodedPayload encodedPayload = mock(EncodedPayload.class);
    when(encodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.MANDATORY_RECIPIENTS);
    when(encodedPayload.getRecipientKeys()).thenReturn(List.of(recipient1));
    when(encodedPayload.getMandatoryRecipients()).thenReturn(Set.of(recipient1));

    final AffectedTransaction affectedTransaction = mock(AffectedTransaction.class);
    when(affectedTransaction.getPayload()).thenReturn(encodedPayload);
    final TxHash hash = TxHash.from("someHash".getBytes());
    when(affectedTransaction.getHash()).thenReturn(hash);

    boolean valid =
        privacyHelper.validateSendRequest(
            PrivacyMode.MANDATORY_RECIPIENTS,
            List.of(recipient1, recipient2),
            singletonList(affectedTransaction),
            Set.of(recipient1, recipient2));

    assertThat(valid).isTrue();
  }
}
