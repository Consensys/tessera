package com.quorum.tessera.data.staging;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StagingAffectedTransactionTest {

    @Test
    public void twoAffectedContractTransactionWithSameIdAreEqual() {

        StagingTransaction stagingTransaction = new StagingTransaction();
        stagingTransaction.setHash("TXNHASH");

        StagingAffectedTransaction affectedContractTransaction = new StagingAffectedTransaction();
        affectedContractTransaction.setHash("HASH1");
        affectedContractTransaction.setSourceTransaction(stagingTransaction);

        StagingAffectedTransaction anotherAffectedContractTransaction = new StagingAffectedTransaction();
        anotherAffectedContractTransaction.setHash("HASH1");
        anotherAffectedContractTransaction.setSourceTransaction(stagingTransaction);

        assertThat(affectedContractTransaction).isEqualTo(affectedContractTransaction);
        assertThat(affectedContractTransaction).hasSameHashCodeAs(anotherAffectedContractTransaction);
        assertThat(affectedContractTransaction).isEqualTo(anotherAffectedContractTransaction);
        assertThat(affectedContractTransaction).isNotEqualTo(new Object());
    }


}
