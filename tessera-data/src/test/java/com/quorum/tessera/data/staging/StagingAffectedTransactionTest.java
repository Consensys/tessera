package com.quorum.tessera.data.staging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.Test;

public class StagingAffectedTransactionTest {

  @Test
  public void twoAffectedContractTransactionWithSameIdAreEqual() {

    StagingTransaction stagingTransaction = new StagingTransaction();
    stagingTransaction.setHash("TXNHASH");
    StagingTransaction stagingTransaction2 = new StagingTransaction();
    stagingTransaction2.setHash("TXNHASH2");

    StagingAffectedTransaction affectedContractTransaction = new StagingAffectedTransaction();
    affectedContractTransaction.setHash("HASH1");
    affectedContractTransaction.setSourceTransaction(stagingTransaction);

    StagingAffectedTransaction anotherAffectedContractTransaction =
        new StagingAffectedTransaction();
    anotherAffectedContractTransaction.setHash("HASH1");
    anotherAffectedContractTransaction.setSourceTransaction(stagingTransaction);

    StagingAffectedTransaction differentHash = new StagingAffectedTransaction();
    differentHash.setHash("HASH2");
    differentHash.setSourceTransaction(stagingTransaction);

    StagingAffectedTransaction differentSourceTx = new StagingAffectedTransaction();
    differentSourceTx.setHash("HASH1");
    differentSourceTx.setSourceTransaction(stagingTransaction2);

    assertThat(affectedContractTransaction).isEqualTo(affectedContractTransaction);
    assertThat(affectedContractTransaction).hasSameHashCodeAs(anotherAffectedContractTransaction);
    assertThat(affectedContractTransaction).isEqualTo(anotherAffectedContractTransaction);
    assertThat(affectedContractTransaction).isNotEqualTo(new Object());
    assertThat(affectedContractTransaction).isNotEqualTo(null);
    assertThat(affectedContractTransaction).isNotEqualTo(differentHash);
    assertThat(affectedContractTransaction).isNotEqualTo(differentSourceTx);

    assertThat(affectedContractTransaction.equals(affectedContractTransaction)).isTrue();
    assertThat(affectedContractTransaction.equals(null)).isFalse();
    assertThat(affectedContractTransaction.equals(Map.of())).isFalse();
  }
}
