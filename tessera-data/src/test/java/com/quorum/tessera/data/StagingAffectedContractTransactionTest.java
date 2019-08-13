package com.quorum.tessera.data;


import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StagingAffectedContractTransactionTest {

    @Test
    public void idEquals() {
        StagingAffectedContractTransaction obj = new StagingAffectedContractTransaction();
        obj.setSecurityHash("securityHash".getBytes());
        obj.setSourceTransaction(new StagingTransaction());
        StagingAffectedContractTransactionId id = new StagingAffectedContractTransactionId();
        final MessageHashStr source = new MessageHashStr("source".getBytes());
        id.setSource(source);
        final MessageHashStr affected = new MessageHashStr("affected".getBytes());
        id.setAffected(affected);
        obj.setId(id);
        StagingTransaction st = new StagingTransaction();
        obj.setSourceTransaction(st);

        assertThat(obj.getSecurityHash()).isEqualTo("securityHash".getBytes());
        assertThat(obj.getSourceTransaction()).isEqualTo(st);


        StagingAffectedContractTransaction obj2 = new StagingAffectedContractTransaction();
        obj2.setId(id);

        assertThat(obj.equals(obj)).isTrue();
        assertThat(obj.hashCode()).isEqualTo(obj2.hashCode());
        assertThat(obj.equals(obj2)).isTrue();
        assertThat(obj.equals(new Object())).isFalse();
        assertThat(obj.affected()).isSameAs(affected);

    }

}
