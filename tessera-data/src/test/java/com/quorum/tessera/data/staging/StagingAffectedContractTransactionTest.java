package com.quorum.tessera.data.staging;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StagingAffectedContractTransactionTest {

    @Test
    public void twoAffectedContractTransactionWithSameIdAreEqual() {
        StagingAffectedContractTransaction affectedContractTransaction = new StagingAffectedContractTransaction();
        affectedContractTransaction.setId(1L);

        StagingAffectedContractTransaction anotherAffectedContractTransaction = new StagingAffectedContractTransaction();
        anotherAffectedContractTransaction.setId(1L);

        assertThat(affectedContractTransaction).isEqualTo(affectedContractTransaction);
        assertThat(affectedContractTransaction).hasSameHashCodeAs(anotherAffectedContractTransaction);
        assertThat(affectedContractTransaction).isEqualTo(anotherAffectedContractTransaction);
        assertThat(affectedContractTransaction).isNotEqualTo(new Object());
    }

    @Test
    public void nullIdsAreNotEqual() {
        StagingAffectedContractTransaction affectedContractTransaction
            = new StagingAffectedContractTransaction();
        StagingAffectedContractTransaction anotherAffectedContractTransaction = new StagingAffectedContractTransaction();

        assertThat(affectedContractTransaction).isNotEqualTo(anotherAffectedContractTransaction);

    }
}
