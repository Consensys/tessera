package com.quorum.tessera.data.staging;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StagingAffectedContractTransactionIdTest {

    @Test
    public void testEquals() {
        MessageHashStr source = new MessageHashStr("source".getBytes());
        MessageHashStr affected = new MessageHashStr("affected".getBytes());
        StagingAffectedContractTransactionId id1 = new StagingAffectedContractTransactionId(source, affected);
        StagingAffectedContractTransactionId id2 = new StagingAffectedContractTransactionId(source, affected);
        StagingAffectedContractTransactionId id3 =
                new StagingAffectedContractTransactionId(source, new MessageHashStr());
        StagingAffectedContractTransactionId id4 =
                new StagingAffectedContractTransactionId(new MessageHashStr(), affected);

        assertThat(id1.equals(id2)).isTrue();
        assertThat(id1.equals(new Object())).isFalse();
        assertThat(id1.equals(id3)).isFalse();
        assertThat(id1.equals(id4)).isFalse();
    }
}
