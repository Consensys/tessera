package com.quorum.tessera.multitenancy.migration;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.PublicKey;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

public class EncryptedTransactionMigratorTest {

    private EncryptedTransactionDAO primaryDao;

    private EncryptedTransactionDAO secondaryDao;

    private PayloadEncoder payloadEncoder;

    private EncryptedTransactionMigrator migrator;

    @Before
    public void init() {
        this.primaryDao = mock(EncryptedTransactionDAO.class);
        this.secondaryDao = mock(EncryptedTransactionDAO.class);
        this.payloadEncoder = mock(PayloadEncoder.class);

        this.migrator = new EncryptedTransactionMigrator(primaryDao, secondaryDao, payloadEncoder);
    }

    @After
    public void after() {
        verifyNoMoreInteractions(primaryDao, secondaryDao);
    }

    @Test
    public void singleBatchOnlyCallsOnce() {
        final MessageHash testTxHash = new MessageHash("testHash".getBytes());
        final EncryptedTransaction testTx = new EncryptedTransaction();
        testTx.setHash(testTxHash);

        when(secondaryDao.transactionCount()).thenReturn(1L);
        when(secondaryDao.retrieveTransactions(0, 100)).thenReturn(List.of(testTx));
        when(primaryDao.retrieveByHash(testTxHash)).thenReturn(Optional.empty());

        migrator.migrate();

        verify(secondaryDao).transactionCount();
        verify(secondaryDao).retrieveTransactions(0, 100);
        verify(primaryDao).retrieveByHash(testTxHash);
        verify(primaryDao).save(testTx);
    }

    @Test
    public void multipleBatchesForLargeCounts() {
        final MessageHash testTxHash = new MessageHash("testHash".getBytes());
        final EncryptedTransaction testTx = new EncryptedTransaction();
        testTx.setHash(testTxHash);

        when(secondaryDao.transactionCount()).thenReturn(201L);
        when(secondaryDao.retrieveTransactions(0, 100)).thenReturn(List.of(testTx));
        when(secondaryDao.retrieveTransactions(100, 100)).thenReturn(List.of(testTx));
        when(secondaryDao.retrieveTransactions(200, 100)).thenReturn(List.of(testTx));
        when(primaryDao.retrieveByHash(testTxHash)).thenReturn(Optional.empty());

        migrator.migrate();

        verify(secondaryDao).transactionCount();
        verify(secondaryDao).retrieveTransactions(0, 100);
        verify(secondaryDao).retrieveTransactions(100, 100);
        verify(secondaryDao).retrieveTransactions(200, 100);
        verify(primaryDao, times(3)).retrieveByHash(testTxHash);
        verify(primaryDao, times(3)).save(testTx);
    }

    @Test
    public void jdbcErrorStopsProcessing() {
        final MessageHash testTxHash = new MessageHash("testHash".getBytes());
        final EncryptedTransaction testTx = new EncryptedTransaction();
        testTx.setHash(testTxHash);

        final MessageHash testTxHash2 = new MessageHash("testHash2".getBytes());
        final EncryptedTransaction testTx2 = new EncryptedTransaction();
        testTx2.setHash(testTxHash2);

        when(secondaryDao.transactionCount()).thenReturn(2L);
        when(secondaryDao.retrieveTransactions(0, 100)).thenReturn(List.of(testTx, testTx2));
        when(primaryDao.retrieveByHash(testTxHash)).thenThrow(RuntimeException.class);

        final Throwable throwable = catchThrowable(migrator::migrate);

        assertThat(throwable).isInstanceOf(RuntimeException.class);

        verify(secondaryDao).transactionCount();
        verify(secondaryDao).retrieveTransactions(0, 100);
        verify(primaryDao).retrieveByHash(testTxHash);
    }

    @Test
    public void txExistsInBothUpdatesPrimary() {
        // PSV tx that exists in both, where the tx from the secondary db
        // should overwrite the primary
        final PublicKey sender = PublicKey.from("sender".getBytes());
        final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
        final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());
        final byte[] recipient1Box = "box1".getBytes();

        final EncodedPayload secondaryPayload =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
                        .withSenderKey(sender)
                        .withNewRecipientKeys(List.of(sender, recipient1, recipient2))
                        .withRecipientBoxes(List.of("boxSender".getBytes(), recipient1Box, "box2".getBytes()))
                        .build();
        final EncodedPayload primaryPayload =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
                        .withSenderKey(sender)
                        .withNewRecipientKeys(List.of(recipient1, sender, recipient2))
                        .withRecipientBox(recipient1Box)
                        .build();

        final MessageHash txHash = new MessageHash("testHash".getBytes());
        final EncryptedTransaction primaryDbTx = new EncryptedTransaction();
        primaryDbTx.setHash(txHash);
        primaryDbTx.setEncodedPayload("payload1".getBytes());
        final EncryptedTransaction secondaryDbTx = new EncryptedTransaction();
        secondaryDbTx.setHash(txHash);
        secondaryDbTx.setEncodedPayload("payload2".getBytes());

        when(payloadEncoder.decode("payload1".getBytes())).thenReturn(primaryPayload);
        when(payloadEncoder.decode("payload2".getBytes())).thenReturn(secondaryPayload);
        when(payloadEncoder.encode(secondaryPayload)).thenReturn("updatedPayload".getBytes());
        when(secondaryDao.transactionCount()).thenReturn(1L);
        when(secondaryDao.retrieveTransactions(0, 100)).thenReturn(List.of(secondaryDbTx));
        when(primaryDao.retrieveByHash(txHash)).thenReturn(Optional.of(primaryDbTx));

        migrator.migrate();

        // migrator updates the primary tx in place, so we can see the updated value on the existing object
        assertThat(new String(primaryDbTx.getEncodedPayload())).isEqualTo("updatedPayload");

        verify(payloadEncoder).decode("payload1".getBytes());
        verify(payloadEncoder).decode("payload2".getBytes());
        verify(payloadEncoder).encode(secondaryPayload);
        verify(secondaryDao).transactionCount();
        verify(secondaryDao).retrieveTransactions(0, 100);
        verify(primaryDao).retrieveByHash(txHash);
        verify(primaryDao).update(primaryDbTx);
    }

    @Test
    public void psvTxWithPrimaryAsSender() {
        final PublicKey sender = PublicKey.from("sender".getBytes());
        final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
        final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());
        final byte[] recipient1Box = "box1".getBytes();

        final EncodedPayload primaryPayload =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
                        .withSenderKey(sender)
                        .withNewRecipientKeys(List.of(sender, recipient1, recipient2))
                        .withRecipientBoxes(List.of("boxSender".getBytes(), recipient1Box, "box2".getBytes()))
                        .build();
        final EncodedPayload secondaryPayload =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
                        .withSenderKey(sender)
                        .withNewRecipientKeys(List.of(recipient1, sender, recipient2))
                        .withRecipientBox(recipient1Box)
                        .build();

        final EncodedPayload result = migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

        assertThat(result).isEqualToComparingFieldByFieldRecursively(primaryPayload);
    }

    @Test
    public void psvTxWithSecondaryAsSender() {
        final PublicKey sender = PublicKey.from("sender".getBytes());
        final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
        final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());
        final byte[] recipient1Box = "box1".getBytes();

        final EncodedPayload secondaryPayload =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
                        .withSenderKey(sender)
                        .withNewRecipientKeys(List.of(sender, recipient1, recipient2))
                        .withRecipientBoxes(List.of("boxSender".getBytes(), recipient1Box, "box2".getBytes()))
                        .build();
        final EncodedPayload primaryPayload =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
                        .withSenderKey(sender)
                        .withNewRecipientKeys(List.of(recipient1, sender, recipient2))
                        .withRecipientBox(recipient1Box)
                        .build();

        final EncodedPayload result = migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

        assertThat(result).isEqualToComparingFieldByFieldRecursively(secondaryPayload);
    }

    @Test
    public void psvTxWithBothAsRecipients() {
        final PublicKey sender = PublicKey.from("sender".getBytes());
        final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
        final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());
        final byte[] recipient1Box = "box1".getBytes();
        final byte[] recipient2Box = "box2".getBytes();
        final Map<TxHash, byte[]> recipient1Acoths =
                Map.of(TxHash.from("txhash1".getBytes()), "securityhash1".getBytes());
        final Map<TxHash, byte[]> recipient2Acoths =
                Map.of(TxHash.from("txhash2".getBytes()), "securityhash2".getBytes());

        final EncodedPayload primaryPayload =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
                        .withSenderKey(sender)
                        .withNewRecipientKeys(List.of(recipient1, sender, recipient2))
                        .withRecipientBoxes(List.of(recipient1Box))
                        .withAffectedContractTransactions(recipient1Acoths)
                        .build();
        final EncodedPayload secondaryPayload =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
                        .withSenderKey(sender)
                        .withNewRecipientKeys(List.of(recipient2, sender, recipient1))
                        .withRecipientBox(recipient2Box)
                        .withAffectedContractTransactions(recipient2Acoths)
                        .build();

        final EncodedPayload result = migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

        final EncodedPayload expected =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
                        .withSenderKey(sender)
                        .withNewRecipientKeys(List.of(recipient2, recipient1, sender))
                        .withRecipientBoxes(List.of(recipient2Box, recipient1Box))
                        .withAffectedContractTransactions(
                                Map.of(
                                        TxHash.from("txhash1".getBytes()), "securityhash1".getBytes(),
                                        TxHash.from("txhash2".getBytes()), "securityhash2".getBytes()))
                        .build();

        assertThat(result).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    public void ppTxWithPrimaryAsSender() {
        final PublicKey sender = PublicKey.from("sender".getBytes());
        final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
        final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());
        final byte[] recipient1Box = "box1".getBytes();

        final EncodedPayload primaryPayload =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.PARTY_PROTECTION)
                        .withSenderKey(sender)
                        .withNewRecipientKeys(List.of(sender, recipient1, recipient2))
                        .withRecipientBoxes(List.of("boxSender".getBytes(), recipient1Box, "box2".getBytes()))
                        .build();
        final EncodedPayload secondaryPayload =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.PARTY_PROTECTION)
                        .withSenderKey(sender)
                        .withNewRecipientKeys(List.of(recipient1))
                        .withRecipientBox(recipient1Box)
                        .build();

        final EncodedPayload result = migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

        assertThat(result).isEqualToComparingFieldByFieldRecursively(primaryPayload);
    }

    @Test
    public void ppTxWithSecondaryAsSender() {
        final PublicKey sender = PublicKey.from("sender".getBytes());
        final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
        final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());
        final byte[] recipient1Box = "box1".getBytes();

        final EncodedPayload secondaryPayload =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.PARTY_PROTECTION)
                        .withSenderKey(sender)
                        .withNewRecipientKeys(List.of(sender, recipient1, recipient2))
                        .withRecipientBoxes(List.of("boxSender".getBytes(), recipient1Box, "box2".getBytes()))
                        .build();
        final EncodedPayload primaryPayload =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.PARTY_PROTECTION)
                        .withSenderKey(sender)
                        .withNewRecipientKeys(List.of(recipient1))
                        .withRecipientBox(recipient1Box)
                        .build();

        final EncodedPayload result = migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

        assertThat(result).isEqualToComparingFieldByFieldRecursively(secondaryPayload);
    }

    @Test
    public void ppTxWithBothAsRecipients() {
        final PublicKey sender = PublicKey.from("sender".getBytes());
        final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
        final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());
        final byte[] recipient1Box = "box1".getBytes();
        final byte[] recipient2Box = "box2".getBytes();

        final EncodedPayload primaryPayload =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.PARTY_PROTECTION)
                        .withSenderKey(sender)
                        .withNewRecipientKeys(List.of(recipient1))
                        .withRecipientBoxes(List.of(recipient1Box))
                        .build();
        final EncodedPayload secondaryPayload =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.PARTY_PROTECTION)
                        .withSenderKey(sender)
                        .withNewRecipientKeys(List.of(recipient2))
                        .withRecipientBox(recipient2Box)
                        .build();

        final EncodedPayload result = migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

        final EncodedPayload expected =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.PARTY_PROTECTION)
                        .withSenderKey(sender)
                        .withNewRecipientKeys(List.of(recipient1, recipient2))
                        .withRecipientBoxes(List.of(recipient1Box, recipient2Box))
                        .build();

        assertThat(result).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    public void spPETxWithPrimaryAsSender() {
        final PublicKey sender = PublicKey.from("sender".getBytes());
        final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
        final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());
        final byte[] recipient1Box = "box1".getBytes();

        final EncodedPayload primaryPayload =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withSenderKey(sender)
                        .withNewRecipientKeys(List.of(sender, recipient1, recipient2))
                        .withRecipientBoxes(List.of("boxSender".getBytes(), recipient1Box, "box2".getBytes()))
                        .build();
        final EncodedPayload secondaryPayload =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withSenderKey(sender)
                        .withNewRecipientKeys(List.of(recipient1))
                        .withRecipientBox(recipient1Box)
                        .build();

        final EncodedPayload result = migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

        assertThat(result).isEqualToComparingFieldByFieldRecursively(primaryPayload);
    }

    @Test
    public void spPrePETxWithPrimaryAsSender() {
        final PublicKey sender = PublicKey.from("sender".getBytes());
        final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
        final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());
        final byte[] recipient1Box = "box1".getBytes();

        final EncodedPayload primaryPayload =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withSenderKey(sender)
                        .withNewRecipientKeys(List.of(sender, recipient1, recipient2))
                        .withRecipientBoxes(List.of("boxSender".getBytes(), recipient1Box, "box2".getBytes()))
                        .build();
        final EncodedPayload secondaryPayload =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withSenderKey(sender)
                        .withRecipientBox(recipient1Box)
                        .build();

        final EncodedPayload result = migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

        assertThat(result).isEqualToComparingFieldByFieldRecursively(primaryPayload);
    }

    @Test
    public void spPETxWithSecondaryAsSender() {
        final PublicKey sender = PublicKey.from("sender".getBytes());
        final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
        final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());
        final byte[] recipient1Box = "box1".getBytes();

        final EncodedPayload secondaryPayload =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withSenderKey(sender)
                        .withNewRecipientKeys(List.of(sender, recipient1, recipient2))
                        .withRecipientBoxes(List.of("boxSender".getBytes(), recipient1Box, "box2".getBytes()))
                        .build();
        final EncodedPayload primaryPayload =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withSenderKey(sender)
                        .withNewRecipientKeys(List.of(recipient1))
                        .withRecipientBox(recipient1Box)
                        .build();

        final EncodedPayload result = migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

        assertThat(result).isEqualToComparingFieldByFieldRecursively(secondaryPayload);
    }

    @Test
    public void spPrePETxWithSecondaryAsSender() {
        final PublicKey sender = PublicKey.from("sender".getBytes());
        final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
        final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());
        final byte[] recipient1Box = "box1".getBytes();

        final EncodedPayload secondaryPayload =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withSenderKey(sender)
                        .withNewRecipientKeys(List.of(sender, recipient1, recipient2))
                        .withRecipientBoxes(List.of("boxSender".getBytes(), recipient1Box, "box2".getBytes()))
                        .build();
        final EncodedPayload primaryPayload =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withSenderKey(sender)
                        .withRecipientBox(recipient1Box)
                        .build();

        final EncodedPayload result = migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

        assertThat(result).isEqualToComparingFieldByFieldRecursively(secondaryPayload);
    }

    @Test
    public void spPETxWithBothRecipients() {
        final PublicKey sender = PublicKey.from("sender".getBytes());
        final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
        final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());
        final byte[] recipient1Box = "box1".getBytes();
        final byte[] recipient2Box = "box2".getBytes();

        final EncodedPayload primaryPayload =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withSenderKey(sender)
                        .withRecipientKey(recipient1)
                        .withRecipientBox(recipient1Box)
                        .build();
        final EncodedPayload secondaryPayload =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withSenderKey(sender)
                        .withRecipientKey(recipient2)
                        .withRecipientBox(recipient2Box)
                        .build();

        final EncodedPayload result = migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

        final EncodedPayload expected =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withSenderKey(sender)
                        .withRecipientKeys(List.of(recipient1, recipient2))
                        .withRecipientBoxes(List.of(recipient1Box, recipient2Box))
                        .build();

        assertThat(result).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    public void spPrePETxWithBothRecipients() {
        final PublicKey sender = PublicKey.from("sender".getBytes());
        final byte[] recipient1Box = "box1".getBytes();
        final byte[] recipient2Box = "box2".getBytes();

        final EncodedPayload primaryPayload =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withSenderKey(sender)
                        .withRecipientBoxes(List.of(recipient1Box))
                        .build();
        final EncodedPayload secondaryPayload =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withSenderKey(sender)
                        .withRecipientBox(recipient2Box)
                        .build();

        final EncodedPayload result = migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

        final EncodedPayload expected =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withSenderKey(sender)
                        .withRecipientBoxes(List.of(recipient1Box, recipient2Box))
                        .build();

        assertThat(result).isEqualToComparingFieldByFieldRecursively(expected);
    }

    // From a pre-0.8 tx
    @Test
    public void spPrimarySenderDoesntHaveOwnKeyInList() {
        final PublicKey sender = PublicKey.from("sender".getBytes());
        final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
        final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());
        final byte[] recipient1Box = "box1".getBytes();

        final EncodedPayload primaryPayload =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withSenderKey(sender)
                        .withNewRecipientKeys(List.of(recipient1, recipient2))
                        .withRecipientBoxes(List.of(recipient1Box, "box2".getBytes()))
                        .build();
        final EncodedPayload secondaryPayload =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withSenderKey(sender)
                        .withRecipientBox(recipient1Box)
                        .build();

        final EncodedPayload result = migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

        assertThat(result).isEqualToComparingFieldByFieldRecursively(primaryPayload);
    }

    // From a pre-0.8 tx
    @Test
    public void spSecondarySenderDoesntHaveOwnKeyInList() {
        final PublicKey sender = PublicKey.from("sender".getBytes());
        final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
        final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());
        final byte[] recipient1Box = "box1".getBytes();

        final EncodedPayload secondaryPayload =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withSenderKey(sender)
                        .withNewRecipientKeys(List.of(recipient1, recipient2))
                        .withRecipientBoxes(List.of(recipient1Box, "box2".getBytes()))
                        .build();
        final EncodedPayload primaryPayload =
                EncodedPayload.Builder.create()
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withSenderKey(sender)
                        .withRecipientBox(recipient1Box)
                        .build();

        final EncodedPayload result = migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

        assertThat(result).isEqualToComparingFieldByFieldRecursively(secondaryPayload);
    }
}
