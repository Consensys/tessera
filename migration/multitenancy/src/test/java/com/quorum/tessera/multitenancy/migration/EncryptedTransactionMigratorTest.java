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

  private PayloadEncoder payloadEncoder;

  private EncryptedTransactionMigrator migrator;

  @Before
  public void init() {
    this.primaryDao = mock(EntityManager.class);
    this.secondaryDao = mock(EntityManager.class);
    this.payloadEncoder = mock(PayloadEncoder.class);

    this.migrator = new EncryptedTransactionMigrator(primaryDao, secondaryDao, payloadEncoder);
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

    RecipientBox senderRecipientBox = mock(RecipientBox.class);
    when(senderRecipientBox.getData()).thenReturn("boxSender".getBytes());

    RecipientBox recipient1Box = mock(RecipientBox.class);
    when(recipient1Box.getData()).thenReturn("box1".getBytes());

    RecipientBox recipient2Box = mock(RecipientBox.class);
    when(recipient2Box.getData()).thenReturn("box1".getBytes());

    final EncodedPayload primaryPayload = mock(EncodedPayload.class);
    when(primaryPayload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
    when(primaryPayload.getExecHash()).thenReturn("execHash".getBytes());
    when(primaryPayload.getSenderKey()).thenReturn(sender);
    when(primaryPayload.getRecipientKeys()).thenReturn(List.of(sender, recipient1, recipient2));
    when(primaryPayload.getRecipientBoxes())
        .thenReturn(List.of(senderRecipientBox, recipient1Box, recipient2Box));

    final EncodedPayload secondaryPayload = mock(EncodedPayload.class);
    when(secondaryPayload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
    when(secondaryPayload.getSenderKey()).thenReturn(sender);
    when(secondaryPayload.getExecHash()).thenReturn("execHash".getBytes());
    when(secondaryPayload.getRecipientKeys()).thenReturn(List.of(recipient1, sender, recipient2));
    when(secondaryPayload.getRecipientBoxes()).thenReturn(List.of(recipient1Box));

    final EncodedPayload result =
        migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

    assertThat(result).isNotNull();
    assertThat(result.getRecipientKeys()).contains(recipient1, sender, recipient2);
    assertThat(result.getSenderKey()).isEqualTo(sender);
    assertThat(result.getExecHash()).containsExactly("execHash".getBytes());
    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.PRIVATE_STATE_VALIDATION);
    assertThat(result.getRecipientBoxes())
        .containsExactly(senderRecipientBox, recipient1Box, recipient2Box);
  }

  @Test
  public void psvTxWithSecondaryAsSender() {
    final PublicKey sender = PublicKey.from("sender".getBytes());
    final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
    final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());

    RecipientBox senderRecipientBox = mock(RecipientBox.class);
    when(senderRecipientBox.getData()).thenReturn("boxSender".getBytes());

    RecipientBox recipient1Box = mock(RecipientBox.class);
    when(recipient1Box.getData()).thenReturn("box1".getBytes());

    RecipientBox recipient2Box = mock(RecipientBox.class);
    when(recipient2Box.getData()).thenReturn("box2".getBytes());

    final EncodedPayload secondaryPayload = mock(EncodedPayload.class);
    when(secondaryPayload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
    when(secondaryPayload.getExecHash()).thenReturn("execHash".getBytes());
    when(secondaryPayload.getSenderKey()).thenReturn(sender);
    when(secondaryPayload.getRecipientKeys()).thenReturn(List.of(sender, recipient1, recipient2));
    when(secondaryPayload.getRecipientBoxes())
        .thenReturn(List.of(senderRecipientBox, recipient1Box, recipient2Box));

    final EncodedPayload primaryPayload = mock(EncodedPayload.class);
    when(primaryPayload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
    when(primaryPayload.getExecHash()).thenReturn("execHash".getBytes());
    when(primaryPayload.getSenderKey()).thenReturn(sender);
    when(primaryPayload.getRecipientKeys()).thenReturn(List.of(recipient1, sender, recipient2));
    when(primaryPayload.getRecipientBoxes()).thenReturn(List.of(recipient1Box));

    final EncodedPayload result =
        migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

    assertThat(result).isNotNull();
    assertThat(result.getRecipientKeys()).contains(recipient1, sender, recipient2);
    assertThat(result.getSenderKey()).isEqualTo(sender);
    assertThat(result.getExecHash()).containsExactly("execHash".getBytes());
    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.PRIVATE_STATE_VALIDATION);
    assertThat(result.getRecipientBoxes())
        .containsExactly(senderRecipientBox, recipient1Box, recipient2Box);
  }

  @Test
  public void psvTxWithBothAsRecipients() {
    final PublicKey sender = PublicKey.from("sender".getBytes());
    final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
    final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());

    final RecipientBox recipient1Box = mock(RecipientBox.class);
    when(recipient1Box.getData()).thenReturn("box1".getBytes());

    final RecipientBox recipient2Box = mock(RecipientBox.class);
    when(recipient2Box.getData()).thenReturn("box2".getBytes());

    final Map<TxHash, SecurityHash> recipient1Acoths =
        Map.of(TxHash.from("txhash1".getBytes()), SecurityHash.from("securityhash1".getBytes()));

    final Map<TxHash, SecurityHash> recipient2Acoths =
        Map.of(TxHash.from("txhash2".getBytes()), SecurityHash.from("securityhash2".getBytes()));

    final EncodedPayload primaryPayload = mock(EncodedPayload.class);
    when(primaryPayload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
    when(primaryPayload.getExecHash()).thenReturn("execHash".getBytes());
    when(primaryPayload.getSenderKey()).thenReturn(sender);
    when(primaryPayload.getRecipientKeys()).thenReturn(List.of(recipient1, sender, recipient2));
    when(primaryPayload.getRecipientBoxes()).thenReturn(List.of(recipient1Box));
    when(primaryPayload.getAffectedContractTransactions()).thenReturn(recipient1Acoths);

    final EncodedPayload secondaryPayload = mock(EncodedPayload.class);
    when(secondaryPayload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
    when(secondaryPayload.getExecHash()).thenReturn("execHash".getBytes());
    when(secondaryPayload.getSenderKey()).thenReturn(sender);
    when(secondaryPayload.getRecipientKeys()).thenReturn(List.of(recipient2, sender, recipient1));
    when(secondaryPayload.getRecipientBoxes()).thenReturn(List.of(recipient2Box));
    when(secondaryPayload.getAffectedContractTransactions()).thenReturn(recipient2Acoths);

    final EncodedPayload result =
        migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

    assertThat(result).isNotNull();
    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.PRIVATE_STATE_VALIDATION);
    assertThat(result.getExecHash()).isEqualTo("execHash".getBytes());
    assertThat(result.getSenderKey()).isEqualTo(sender);
    assertThat(result.getRecipientKeys()).containsAll(List.of(recipient2, recipient1, sender));
    assertThat(result.getRecipientBoxes()).containsAll(List.of(recipient2Box, recipient1Box));
    assertThat(result.getAffectedContractTransactions()).hasSize(2);
    assertThat(result.getAffectedContractTransactions()).containsAllEntriesOf(recipient1Acoths);
    assertThat(result.getAffectedContractTransactions()).containsAllEntriesOf(recipient2Acoths);
  }

  @Test
  public void ppTxWithPrimaryAsSender() {
    final PublicKey sender = PublicKey.from("sender".getBytes());
    final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
    final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());

    RecipientBox senderRecipientBox = mock(RecipientBox.class);
    when(senderRecipientBox.getData()).thenReturn("boxSender".getBytes());

    final RecipientBox recipient1Box = mock(RecipientBox.class);
    when(recipient1Box.getData()).thenReturn("box1".getBytes());

    final RecipientBox recipient2Box = mock(RecipientBox.class);
    when(recipient2Box.getData()).thenReturn("box2".getBytes());

    final EncodedPayload primaryPayload = mock(EncodedPayload.class);
    when(primaryPayload.getPrivacyMode()).thenReturn(PrivacyMode.PARTY_PROTECTION);
    when(primaryPayload.getSenderKey()).thenReturn(sender);
    when(primaryPayload.getRecipientKeys()).thenReturn(List.of(sender, recipient1, recipient2));
    when(primaryPayload.getRecipientBoxes())
        .thenReturn(List.of(senderRecipientBox, recipient1Box, recipient2Box));

    final EncodedPayload secondaryPayload = mock(EncodedPayload.class);
    when(secondaryPayload.getPrivacyMode()).thenReturn(PrivacyMode.PARTY_PROTECTION);
    when(secondaryPayload.getSenderKey()).thenReturn(sender);
    when(secondaryPayload.getRecipientKeys()).thenReturn(List.of(recipient1));
    when(secondaryPayload.getRecipientBoxes()).thenReturn(List.of(recipient1Box));

    final EncodedPayload result =
        migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

    assertThat(result).isNotNull();
    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.PARTY_PROTECTION);
    assertThat(result.getSenderKey()).isEqualTo(sender);
    assertThat(result.getRecipientKeys()).containsAll(List.of(sender, recipient1, recipient2));
    assertThat(result.getRecipientBoxes())
        .containsAll(List.of(senderRecipientBox, recipient1Box, recipient2Box));
  }

  @Test
  public void ppTxWithSecondaryAsSender() {

    final PublicKey sender = PublicKey.from("sender".getBytes());

    final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());

    final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());

    RecipientBox senderRecipientBox = mock(RecipientBox.class);
    when(senderRecipientBox.getData()).thenReturn("boxSender".getBytes());

    final RecipientBox recipient1Box = mock(RecipientBox.class);
    when(recipient1Box.getData()).thenReturn("box1".getBytes());

    final RecipientBox recipient2Box = mock(RecipientBox.class);
    when(recipient2Box.getData()).thenReturn("box2".getBytes());

    final EncodedPayload secondaryPayload = mock(EncodedPayload.class);
    when(secondaryPayload.getPrivacyMode()).thenReturn(PrivacyMode.PARTY_PROTECTION);
    when(secondaryPayload.getSenderKey()).thenReturn(sender);
    when(secondaryPayload.getRecipientKeys()).thenReturn(List.of(sender, recipient1, recipient2));
    when(secondaryPayload.getRecipientBoxes())
        .thenReturn(List.of(senderRecipientBox, recipient1Box, recipient2Box));

    final EncodedPayload primaryPayload = mock(EncodedPayload.class);
    when(primaryPayload.getPrivacyMode()).thenReturn(PrivacyMode.PARTY_PROTECTION);
    when(primaryPayload.getSenderKey()).thenReturn(sender);
    when(primaryPayload.getRecipientKeys()).thenReturn(List.of(recipient1));
    when(primaryPayload.getRecipientBoxes()).thenReturn(List.of(recipient1Box));

    final EncodedPayload result =
        migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

    assertThat(result).isNotNull();
    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.PARTY_PROTECTION);
    assertThat(result.getSenderKey()).isEqualTo(sender);
    assertThat(result.getRecipientKeys()).containsAll(List.of(sender, recipient1, recipient2));
    assertThat(result.getRecipientBoxes())
        .containsAll(List.of(senderRecipientBox, recipient1Box, recipient2Box));
  }

  @Test
  public void ppTxWithBothAsRecipients() {
    final PublicKey sender = PublicKey.from("sender".getBytes());
    final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
    final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());

    RecipientBox senderRecipientBox = mock(RecipientBox.class);
    when(senderRecipientBox.getData()).thenReturn("boxSender".getBytes());

    final RecipientBox recipient1Box = mock(RecipientBox.class);
    when(recipient1Box.getData()).thenReturn("box1".getBytes());

    final RecipientBox recipient2Box = mock(RecipientBox.class);
    when(recipient2Box.getData()).thenReturn("box2".getBytes());

    final EncodedPayload primaryPayload = mock(EncodedPayload.class);
    when(primaryPayload.getPrivacyMode()).thenReturn(PrivacyMode.PARTY_PROTECTION);
    when(primaryPayload.getSenderKey()).thenReturn(sender);
    when(primaryPayload.getRecipientKeys()).thenReturn(List.of(recipient1));
    when(primaryPayload.getRecipientBoxes()).thenReturn(List.of(recipient1Box));

    final EncodedPayload secondaryPayload = mock(EncodedPayload.class);
    when(secondaryPayload.getPrivacyMode()).thenReturn(PrivacyMode.PARTY_PROTECTION);
    when(secondaryPayload.getSenderKey()).thenReturn(sender);
    when(secondaryPayload.getRecipientKeys()).thenReturn(List.of(recipient2));
    when(secondaryPayload.getRecipientBoxes()).thenReturn(List.of(recipient2Box));

    final EncodedPayload result =
        migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

    assertThat(result).isNotNull();
    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.PARTY_PROTECTION);
    assertThat(result.getSenderKey()).isEqualTo(sender);
    assertThat(result.getRecipientBoxes()).containsExactlyInAnyOrder(recipient1Box, recipient2Box);
    assertThat(result.getRecipientKeys()).containsExactlyInAnyOrder(recipient1, recipient2);
  }

  @Test
  public void spPETxWithPrimaryAsSender() {

    final PublicKey sender = PublicKey.from("sender".getBytes());
    final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
    final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());

    final RecipientBox senderRecipientBox = mock(RecipientBox.class);
    when(senderRecipientBox.getData()).thenReturn("boxSender".getBytes());

    final RecipientBox recipient1Box = mock(RecipientBox.class);
    when(recipient1Box.getData()).thenReturn("box1".getBytes());

    final RecipientBox recipient2Box = mock(RecipientBox.class);
    when(recipient2Box.getData()).thenReturn("box2".getBytes());

    final EncodedPayload primaryPayload = mock(EncodedPayload.class);
    when(primaryPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(primaryPayload.getSenderKey()).thenReturn(sender);
    when(primaryPayload.getRecipientKeys()).thenReturn(List.of(sender, recipient1, recipient2));
    when(primaryPayload.getRecipientBoxes())
        .thenReturn(List.of(senderRecipientBox, recipient1Box, recipient2Box));

    final EncodedPayload secondaryPayload = mock(EncodedPayload.class);
    when(secondaryPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(secondaryPayload.getSenderKey()).thenReturn(sender);
    when(secondaryPayload.getRecipientKeys()).thenReturn(List.of(recipient1));
    when(secondaryPayload.getRecipientBoxes()).thenReturn(List.of(recipient1Box));

    final EncodedPayload result =
        migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

    assertThat(result).isNotNull();
    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
    assertThat(result.getSenderKey()).isEqualTo(sender);
    assertThat(result.getRecipientBoxes())
        .containsExactlyInAnyOrder(senderRecipientBox, recipient1Box, recipient2Box);
    assertThat(result.getRecipientKeys()).containsExactlyInAnyOrder(sender, recipient1, recipient2);
  }

  @Test
  public void spPrePETxWithPrimaryAsSender() {
    final PublicKey sender = PublicKey.from("sender".getBytes());
    final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
    final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());

    final RecipientBox senderRecipientBox = mock(RecipientBox.class);
    when(senderRecipientBox.getData()).thenReturn("boxSender".getBytes());

    final RecipientBox recipient1Box = mock(RecipientBox.class);
    when(recipient1Box.getData()).thenReturn("box1".getBytes());

    final RecipientBox recipient2Box = mock(RecipientBox.class);
    when(recipient2Box.getData()).thenReturn("box2".getBytes());

    final EncodedPayload primaryPayload = mock(EncodedPayload.class);
    when(primaryPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(primaryPayload.getSenderKey()).thenReturn(sender);
    when(primaryPayload.getRecipientKeys()).thenReturn(List.of(sender, recipient1, recipient2));
    when(primaryPayload.getRecipientBoxes())
        .thenReturn(List.of(senderRecipientBox, recipient1Box, recipient2Box));

    final EncodedPayload secondaryPayload = mock(EncodedPayload.class);
    when(secondaryPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(secondaryPayload.getSenderKey()).thenReturn(sender);
    when(secondaryPayload.getRecipientBoxes()).thenReturn(List.of(recipient1Box));

    final EncodedPayload result =
        migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

    assertThat(result).isNotNull();
    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
    assertThat(result.getSenderKey()).isEqualTo(sender);
    assertThat(result.getRecipientBoxes())
        .containsExactlyInAnyOrder(senderRecipientBox, recipient1Box, recipient2Box);
    assertThat(result.getRecipientKeys()).containsExactlyInAnyOrder(sender, recipient1, recipient2);
  }

  @Test
  public void spPETxWithSecondaryAsSender() {

    final PublicKey sender = PublicKey.from("sender".getBytes());
    final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
    final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());

    RecipientBox senderRecipientBox = mock(RecipientBox.class);
    when(senderRecipientBox.getData()).thenReturn("boxSender".getBytes());

    RecipientBox recipient1Box = mock(RecipientBox.class);
    when(recipient1Box.getData()).thenReturn("box1".getBytes());

    RecipientBox recipient2Box = mock(RecipientBox.class);
    when(recipient2Box.getData()).thenReturn("box2".getBytes());

    final EncodedPayload secondaryPayload = mock(EncodedPayload.class);
    when(secondaryPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(secondaryPayload.getSenderKey()).thenReturn(sender);
    when(secondaryPayload.getRecipientKeys()).thenReturn(List.of(sender, recipient1, recipient2));
    when(secondaryPayload.getRecipientBoxes())
        .thenReturn(List.of(senderRecipientBox, recipient1Box, recipient2Box));

    final EncodedPayload primaryPayload = mock(EncodedPayload.class);
    when(primaryPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(primaryPayload.getSenderKey()).thenReturn(sender);
    when(primaryPayload.getRecipientKeys()).thenReturn(List.of(recipient1));
    when(primaryPayload.getRecipientBoxes()).thenReturn(List.of(recipient1Box));

    final EncodedPayload result =
        migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

    assertThat(result).isNotNull();
    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
    assertThat(result.getRecipientKeys()).containsExactlyInAnyOrder(sender, recipient1, recipient2);
    assertThat(result.getRecipientBoxes())
        .containsExactlyInAnyOrder(senderRecipientBox, recipient1Box, recipient2Box);
    assertThat(result.getSenderKey()).isEqualTo(sender);
  }

  @Test
  public void spPrePETxWithSecondaryAsSender() {

    final PublicKey sender = PublicKey.from("sender".getBytes());
    final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
    final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());

    RecipientBox senderRecipientBox = mock(RecipientBox.class);
    when(senderRecipientBox.getData()).thenReturn("boxSender".getBytes());

    RecipientBox recipient1Box = mock(RecipientBox.class);
    when(recipient1Box.getData()).thenReturn("box1".getBytes());

    RecipientBox recipient2Box = mock(RecipientBox.class);
    when(recipient2Box.getData()).thenReturn("box2".getBytes());

    final EncodedPayload secondaryPayload = mock(EncodedPayload.class);
    when(secondaryPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(secondaryPayload.getSenderKey()).thenReturn(sender);
    when(secondaryPayload.getRecipientKeys()).thenReturn(List.of(sender, recipient1, recipient2));
    when(secondaryPayload.getRecipientBoxes())
        .thenReturn(List.of(senderRecipientBox, recipient1Box, recipient2Box));

    final EncodedPayload primaryPayload = mock(EncodedPayload.class);
    when(primaryPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(primaryPayload.getSenderKey()).thenReturn(sender);
    when(primaryPayload.getRecipientBoxes()).thenReturn(List.of(recipient1Box));

    final EncodedPayload result =
        migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

    assertThat(result).isNotNull();
    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
    assertThat(result.getRecipientKeys()).containsExactlyInAnyOrder(sender, recipient1, recipient2);
    assertThat(result.getRecipientBoxes())
        .containsExactlyInAnyOrder(senderRecipientBox, recipient1Box, recipient2Box);
    assertThat(result.getSenderKey()).isEqualTo(sender);
  }

  @Test
  public void spPETxWithBothRecipients() {

    final PublicKey sender = PublicKey.from("sender".getBytes());
    final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
    final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());

    RecipientBox recipient1Box = mock(RecipientBox.class);
    when(recipient1Box.getData()).thenReturn("box1".getBytes());

    RecipientBox recipient2Box = mock(RecipientBox.class);
    when(recipient2Box.getData()).thenReturn("box2".getBytes());

    final EncodedPayload primaryPayload = mock(EncodedPayload.class);
    when(primaryPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(primaryPayload.getSenderKey()).thenReturn(sender);
    when(primaryPayload.getRecipientKeys()).thenReturn(List.of(recipient1));
    when(primaryPayload.getRecipientBoxes()).thenReturn(List.of(recipient1Box));

    final EncodedPayload secondaryPayload = mock(EncodedPayload.class);
    when(secondaryPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(secondaryPayload.getSenderKey()).thenReturn(sender);
    when(secondaryPayload.getRecipientKeys()).thenReturn(List.of(recipient2));
    when(secondaryPayload.getRecipientBoxes()).thenReturn(List.of(recipient2Box));

    final EncodedPayload result =
        migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

    assertThat(result).isNotNull();
    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
    assertThat(result.getSenderKey()).isEqualTo(sender);
    assertThat(result.getRecipientKeys()).containsExactlyInAnyOrder(recipient1, recipient2);
    assertThat(result.getRecipientBoxes()).containsExactlyInAnyOrder(recipient1Box, recipient2Box);
  }

  @Test
  public void spPrePETxWithBothRecipients() {
    final PublicKey sender = PublicKey.from("sender".getBytes());

    RecipientBox recipient1Box = mock(RecipientBox.class);
    when(recipient1Box.getData()).thenReturn("box1".getBytes());

    RecipientBox recipient2Box = mock(RecipientBox.class);
    when(recipient2Box.getData()).thenReturn("box2".getBytes());

    final EncodedPayload primaryPayload = mock(EncodedPayload.class);
    when(primaryPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(primaryPayload.getSenderKey()).thenReturn(sender);
    when(primaryPayload.getRecipientBoxes()).thenReturn(List.of(recipient1Box));

    final EncodedPayload secondaryPayload = mock(EncodedPayload.class);
    when(secondaryPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(secondaryPayload.getSenderKey()).thenReturn(sender);
    when(secondaryPayload.getRecipientBoxes()).thenReturn(List.of(recipient2Box));

    final EncodedPayload result =
        migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

    assertThat(result).isNotNull();
    assertThat(result.getSenderKey()).isEqualTo(sender);
    assertThat(result.getRecipientBoxes()).containsExactlyInAnyOrder(recipient1Box, recipient2Box);
    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
  }

  // From a pre-0.8 tx
  @Test
  public void spPrimarySenderDoesntHaveOwnKeyInList() {
    final PublicKey sender = PublicKey.from("sender".getBytes());
    final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
    final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());

    RecipientBox recipient1Box = mock(RecipientBox.class);
    when(recipient1Box.getData()).thenReturn("box1".getBytes());

    RecipientBox recipient2Box = mock(RecipientBox.class);
    when(recipient2Box.getData()).thenReturn("box2".getBytes());

    final EncodedPayload primaryPayload = mock(EncodedPayload.class);
    when(primaryPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(primaryPayload.getSenderKey()).thenReturn(sender);
    when(primaryPayload.getRecipientKeys()).thenReturn(List.of(recipient1, recipient2));
    when(primaryPayload.getRecipientBoxes()).thenReturn(List.of(recipient1Box, recipient2Box));

    final EncodedPayload secondaryPayload = mock(EncodedPayload.class);
    when(secondaryPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(secondaryPayload.getSenderKey()).thenReturn(sender);
    when(secondaryPayload.getRecipientBoxes()).thenReturn(List.of(recipient1Box));

    final EncodedPayload result =
        migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

    assertThat(result).isNotNull();
    assertThat(result.getSenderKey()).isEqualTo(sender);
    assertThat(result.getRecipientKeys()).containsExactlyInAnyOrder(recipient1, recipient2);
    assertThat(result.getRecipientBoxes()).containsExactlyInAnyOrder(recipient1Box, recipient2Box);
    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
  }

  // From a pre-0.8 tx
  @Test
  public void spSecondarySenderDoesntHaveOwnKeyInList() {

    final PublicKey sender = PublicKey.from("sender".getBytes());
    final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
    final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());

    RecipientBox recipient1Box = mock(RecipientBox.class);
    when(recipient1Box.getData()).thenReturn("box1".getBytes());

    RecipientBox recipient2Box = mock(RecipientBox.class);
    when(recipient2Box.getData()).thenReturn("box2".getBytes());

    final EncodedPayload secondaryPayload = mock(EncodedPayload.class);
    when(secondaryPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(secondaryPayload.getSenderKey()).thenReturn(sender);
    when(secondaryPayload.getRecipientKeys()).thenReturn(List.of(recipient1, recipient2));
    when(secondaryPayload.getRecipientBoxes()).thenReturn(List.of(recipient1Box, recipient2Box));

    final EncodedPayload primaryPayload = mock(EncodedPayload.class);
    when(primaryPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(primaryPayload.getSenderKey()).thenReturn(sender);
    when(primaryPayload.getRecipientBoxes()).thenReturn(List.of(recipient1Box));

    final EncodedPayload result =
        migrator.handleSingleTransaction(primaryPayload, secondaryPayload);

    assertThat(result).isNotNull();
    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
    assertThat(result.getSenderKey()).isEqualTo(sender);
    assertThat(result.getRecipientKeys()).containsExactlyInAnyOrder(recipient1, recipient2);
    assertThat(result.getRecipientBoxes()).containsExactlyInAnyOrder(recipient1Box, recipient2Box);
  }
}
