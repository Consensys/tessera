package com.quorum.tessera.multitenancy.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.PublicKey;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EncryptedTransactionMigratorTest {

  private EntityManager primaryDao;

  private EntityManager secondaryDao;

  private EncryptedTransactionMigrator migrator;

  @Before
  public void init() {
    this.primaryDao = mock(EntityManager.class);
    this.secondaryDao = mock(EntityManager.class);

    this.migrator = new EncryptedTransactionMigrator(primaryDao, secondaryDao);
  }

  @After
  public void after() {
    verifyNoMoreInteractions(primaryDao, secondaryDao);
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
            .withExecHash("execHash".getBytes())
            .withSenderKey(sender)
            .withNewRecipientKeys(List.of(sender, recipient1, recipient2))
            .withRecipientBoxes(List.of("boxSender".getBytes(), recipient1Box, "box2".getBytes()))
            .build();
    final EncodedPayload secondaryPayload =
        EncodedPayload.Builder.create()
            .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
            .withExecHash("execHash".getBytes())
            .withSenderKey(sender)
            .withNewRecipientKeys(List.of(recipient1, sender, recipient2))
            .withRecipientBox(recipient1Box)
            .build();

    final EncodedPayload result =
        migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

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
            .withExecHash("execHash".getBytes())
            .withSenderKey(sender)
            .withNewRecipientKeys(List.of(sender, recipient1, recipient2))
            .withRecipientBoxes(List.of("boxSender".getBytes(), recipient1Box, "box2".getBytes()))
            .build();
    final EncodedPayload primaryPayload =
        EncodedPayload.Builder.create()
            .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
            .withExecHash("execHash".getBytes())
            .withSenderKey(sender)
            .withNewRecipientKeys(List.of(recipient1, sender, recipient2))
            .withRecipientBox(recipient1Box)
            .build();

    final EncodedPayload result =
        migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

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
            .withExecHash("execHash".getBytes())
            .withSenderKey(sender)
            .withNewRecipientKeys(List.of(recipient1, sender, recipient2))
            .withRecipientBoxes(List.of(recipient1Box))
            .withAffectedContractTransactions(recipient1Acoths)
            .build();
    final EncodedPayload secondaryPayload =
        EncodedPayload.Builder.create()
            .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
            .withExecHash("execHash".getBytes())
            .withSenderKey(sender)
            .withNewRecipientKeys(List.of(recipient2, sender, recipient1))
            .withRecipientBox(recipient2Box)
            .withAffectedContractTransactions(recipient2Acoths)
            .build();

    final EncodedPayload result =
        migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

    final EncodedPayload expected =
        EncodedPayload.Builder.create()
            .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
            .withExecHash("execHash".getBytes())
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

    final EncodedPayload result =
        migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

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

    final EncodedPayload result =
        migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

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

    final EncodedPayload result =
        migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

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

    final EncodedPayload result =
        migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

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

    final EncodedPayload result =
        migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

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

    final EncodedPayload result =
        migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

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

    final EncodedPayload result =
        migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

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

    final EncodedPayload result =
        migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

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

    final EncodedPayload result =
        migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

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

    final EncodedPayload result =
        migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

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

    final EncodedPayload result =
        migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

    assertThat(result).isEqualToComparingFieldByFieldRecursively(secondaryPayload);
  }
}
