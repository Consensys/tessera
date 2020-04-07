package com.quorum.tessera.data.staging;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StagingTransactionRecipientTest {

    @Test
    public void testEquals() {
        StagingTransactionRecipient recipient1 = new StagingTransactionRecipient();
        StagingTransactionRecipientId id = new StagingTransactionRecipientId();
        id.setHash(new MessageHashStr("hash".getBytes()));
        StagingRecipient stagingRecipient = new StagingRecipient("recipient".getBytes());
        id.setRecipient(stagingRecipient);

        StagingTransaction st = new StagingTransaction();

        recipient1.setId(id);
        recipient1.setBox("box".getBytes());
        recipient1.setInitiator(true);
        recipient1.setTransaction(st);

        StagingTransactionRecipient recipient2 = new StagingTransactionRecipient();
        recipient2.setId(id);

        assertThat(recipient1.getBox()).isEqualTo("box".getBytes());
        assertThat(recipient1.isInitiator()).isTrue();
        assertThat(recipient1.getTransaction()).isEqualTo(st);
        assertThat(recipient1.equals(recipient2)).isTrue();
        assertThat(recipient1.hashCode()).isEqualTo(recipient2.hashCode());
        assertThat(recipient1.equals(recipient2)).isTrue();
        assertThat(recipient1.equals(recipient1)).isTrue();
        assertThat(recipient1.equals(new Object())).isFalse();
        assertThat(recipient1.recipient()).isSameAs(stagingRecipient);
    }
}
