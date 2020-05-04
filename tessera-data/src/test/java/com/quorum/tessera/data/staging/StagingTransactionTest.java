package com.quorum.tessera.data.staging;

import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class StagingTransactionTest {

    @Test
    public void instanceWithSameHashEquals() {
        StagingTransaction stagingTransaction = new StagingTransaction();
        stagingTransaction.setRecipients(Collections.emptySet());
        stagingTransaction.setHash(new MessageHashStr("hash".getBytes()));
        stagingTransaction.setAffectedContractTransactions(Collections.emptyMap());
        stagingTransaction.setVersions(Collections.emptySet());

        StagingTransaction stagingTransaction1 = new StagingTransaction();
        stagingTransaction1.setHash(new MessageHashStr("hash".getBytes()));

        StagingTransaction stagingTransaction2 = new StagingTransaction();
        stagingTransaction2.setHash(new MessageHashStr("newHash".getBytes()));

        assertThat(stagingTransaction.equals(stagingTransaction1)).isTrue();
        assertThat(stagingTransaction.equals(new Object())).isFalse();
        assertThat(stagingTransaction.equals(stagingTransaction2)).isFalse();
    }

    @Test
    public void timestampOnPersist() {
        StagingTransaction st = new StagingTransaction();
        st.onPersist();
        assertThat(st.getTimestamp()).isNotNull();
    }
}
