package com.quorum.tessera.data.staging;

import org.junit.Test;

import java.util.Base64;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

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


        assertThat(stagingTransaction).isEqualTo(stagingTransaction1);
        assertThat(stagingTransaction).isNotEqualTo(new Object());
        assertThat(stagingTransaction).isNotEqualTo(stagingTransaction2);
    }

    @Test
    public void timestampOnPersist() {
        StagingTransaction st = new StagingTransaction();
        st.onPersist();
        assertThat(st.getTimestamp()).isNotNull();
    }
}
