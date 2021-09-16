package com.quorum.tessera.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;
import java.util.Set;
import org.junit.Test;

public class ReceiveResponseTest {

  @Test
  public void fromSomeUnencryptedTransactionData() {
    byte[] someData = "SomeData".getBytes();
    ReceiveResponse result =
        ReceiveResponse.Builder.create()
            .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
            .withUnencryptedTransactionData(someData)
            .withSender(PublicKey.from("sender".getBytes()))
            .build();

    assertThat(result.getUnencryptedTransactionData()).containsExactly(someData);
    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
    assertThat(result.getPrivacyGroupId()).isNotPresent();
  }

  @Test(expected = RuntimeException.class)
  public void execHashRequiredFromPrivacyStateValidation() {
    byte[] someData = "SomeData".getBytes();
    ReceiveResponse.Builder.create()
        .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
        .withUnencryptedTransactionData(someData)
        .withSender(PublicKey.from("sender".getBytes()))
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void privacyModeIsRequired() {
    byte[] someData = "SomeData".getBytes();
    ReceiveResponse.Builder.create()
        .withUnencryptedTransactionData(someData)
        .withSender(PublicKey.from("sender".getBytes()))
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void unencryptedTransactionDataIsRequired() {
    ReceiveResponse.Builder.create()
        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
        .withSender(PublicKey.from("sender".getBytes()))
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void senderIsRequired() {
    byte[] someData = "SomeData".getBytes();
    ReceiveResponse.Builder.create()
        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
        .withUnencryptedTransactionData(someData)
        .build();
  }

  @Test
  public void buildWithEverything() {
    ReceiveResponse result =
        ReceiveResponse.Builder.create()
            .withUnencryptedTransactionData("data".getBytes())
            .withSender(PublicKey.from("sender".getBytes()))
            .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
            .withAffectedTransactions(Set.of(new MessageHash("hash".getBytes())))
            .withExecHash("execHash".getBytes())
            .withManagedParties(Set.of(PublicKey.from("ownKey".getBytes())))
            .withPrivacyGroupId(PrivacyGroup.Id.fromBytes("group".getBytes()))
            .build();

    assertThat(result.getPrivacyGroupId()).isPresent();
    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.PRIVATE_STATE_VALIDATION);
    assertThat(result.getAffectedTransactions())
        .containsExactly(new MessageHash("hash".getBytes()));
    assertThat(result.getExecHash()).isEqualTo("execHash".getBytes());
    assertThat(result.getPrivacyGroupId().get())
        .isEqualTo(PrivacyGroup.Id.fromBytes("group".getBytes()));
    assertThat(result.sender()).isEqualTo(PublicKey.from("sender".getBytes()));
    assertThat(result.getManagedParties()).containsExactly(PublicKey.from("ownKey".getBytes()));
  }
}
