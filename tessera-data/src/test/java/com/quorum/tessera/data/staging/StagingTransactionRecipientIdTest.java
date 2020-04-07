package com.quorum.tessera.data.staging;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StagingTransactionRecipientIdTest {

    @Test
    public void testEquals() {
        MessageHashStr messageHash = new MessageHashStr("hash".getBytes());
        StagingRecipient stagingRecipient = new StagingRecipient();
        final StagingTransactionRecipientId id = new StagingTransactionRecipientId(messageHash, stagingRecipient);

        StagingTransactionRecipientId id2 = new StagingTransactionRecipientId();
        id2.setRecipient(stagingRecipient);
        id2.setHash(messageHash);

        StagingTransactionRecipientId id3 = new StagingTransactionRecipientId();
        id3.setRecipient(stagingRecipient);
        id3.setHash(new MessageHashStr("someotherHash".getBytes()));

        StagingTransactionRecipientId id4 = new StagingTransactionRecipientId();
        id4.setRecipient(new StagingRecipient("other".getBytes()));
        id4.setHash(messageHash);

        assertThat(id.getHash()).isEqualTo(messageHash);
        assertThat(id.equals(new Object())).isFalse();
        assertThat(id.equals(id)).isTrue();
        assertThat(id.equals(id2)).isTrue();
        assertThat(id.equals(id3)).isFalse();
        assertThat(id.equals(id4)).isFalse();
    }
}
