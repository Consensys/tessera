package com.quorum.tessera.data.staging;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StagingTransactionVersionTest {

    @Test
    public void testEquals() {

        final StagingTransaction st = new StagingTransaction();

        StagingTransactionRecipientId id = new StagingTransactionRecipientId();
        StagingRecipient recipient = new StagingRecipient("recipient".getBytes());
        id.setRecipient(recipient);

        StagingTransactionVersion version = new StagingTransactionVersion();
        version.setId(id);
        StagingTransactionVersion version2 = new StagingTransactionVersion();
        version2.setId(id);
        version.setTransaction(st);

        version.onPersist();

        assertThat(version.getTimestamp()).isNotNull();
        assertThat(version.getNanotime()).isNotNull();
        assertThat(version.getPrivacyMode()).isEqualTo((byte) 0);

        assertThat(version.equals(version)).isTrue();
        assertThat(version.equals(version2)).isTrue();
        assertThat(version.equals(new Object())).isFalse();
        assertThat(version.hashCode()).isEqualTo(version2.hashCode());
        assertThat(version.getTransaction()).isEqualTo(st);
        assertThat(version.recipient()).isEqualTo(recipient);
    }
}
