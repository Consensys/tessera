package com.quorum.tessera.data.staging;

import com.quorum.tessera.data.Utils;
import com.quorum.tessera.enclave.PrivacyMode;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StagingTransactionVersionTest {

    @Test
    public void testEquals() {
        MessageHashStr messageHash = Utils.createHashStr();
        final StagingTransaction st = new StagingTransaction();
        st.setHash(messageHash);
        StagingRecipient recipient = new StagingRecipient("recipient".getBytes());
        st.getRecipients().add(recipient);


        StagingTransactionVersion version = new StagingTransactionVersion();
        version.setTransaction(st);
        version.setId(1L);

        StagingTransactionVersion version2 = new StagingTransactionVersion();
        version2.setTransaction(st);
        version2.setId(1L);

        version.onPersist();

        assertThat(version.getTimestamp()).isNotNull();
        assertThat(version.getNanotime()).isNotNull();
        assertThat(version.getPrivacyMode())
            .isEqualTo(PrivacyMode.STANDARD_PRIVATE);

        assertThat(version).isEqualTo(version);
        assertThat(version).isEqualTo(version2);
        assertThat(version).isNotEqualTo(new Object());
        assertThat(version).hasSameHashCodeAs(version2);

        assertThat(version.getTransaction()).isEqualTo(st);

    }

    @Test
    public void nullIdsAreNotEqual() {
        StagingTransactionVersion version = new StagingTransactionVersion();
        StagingTransactionVersion anOtherVersion = new StagingTransactionVersion();

        assertThat(version).isNotEqualTo(anOtherVersion);
    }

}
