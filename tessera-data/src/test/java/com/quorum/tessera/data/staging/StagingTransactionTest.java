package com.quorum.tessera.data.staging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import org.junit.Test;

public class StagingTransactionTest {

  @Test
  public void instanceWithSameHashEquals() {

    Base64.Encoder encoder = Base64.getEncoder();

    StagingTransaction stagingTransaction = new StagingTransaction();
    stagingTransaction.setId(1L);
    stagingTransaction.setHash(encoder.encodeToString("hash".getBytes()));
    stagingTransaction.setAffectedContractTransactions(Collections.emptySet());

    StagingTransaction stagingTransaction1 = new StagingTransaction();
    stagingTransaction1.setId(1L);
    stagingTransaction1.setHash(encoder.encodeToString("hash".getBytes()));

    StagingTransaction stagingTransaction2 = new StagingTransaction();
    stagingTransaction2.setId(2L);
    stagingTransaction2.setHash(encoder.encodeToString("newhash".getBytes()));

    assertThat(stagingTransaction)
        .isEqualTo(stagingTransaction1)
        .hasSameHashCodeAs(stagingTransaction1);

    assertThat(stagingTransaction).isNotEqualTo(new Object());
    assertThat(stagingTransaction).isNotEqualTo(stagingTransaction2);
  }

  @Test
  public void timestampOnPersist() {
    StagingTransaction st = new StagingTransaction();
    st.onPersist();
    assertThat(st.getTimestamp()).isNotNull();
  }

  @Test
  public void equals() {

    StagingTransaction txn = new StagingTransaction();
    txn.setId(1L);
    StagingTransaction otherTxn = new StagingTransaction();
    otherTxn.setId(1L);
    assertThat(txn).isEqualTo(otherTxn);

    assertThat(txn).isNotEqualTo(null);
    assertThat(txn).isNotEqualTo(new StagingTransaction());
    assertThat(new StagingTransaction()).isNotEqualTo(new StagingTransaction());

    assertThat(txn.equals(txn)).isTrue();
    assertThat(txn.equals(null)).isFalse();
    assertThat(txn.equals(Map.of())).isFalse();
  }
}
