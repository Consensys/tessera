package com.quorum.tessera.data.staging;

import org.junit.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

public class StagingRecipientTest {

    @Test
    public void testToString() {
        StagingRecipient stagingRecipient = new StagingRecipient();
        stagingRecipient.setBytes(Base64.getDecoder().decode("BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo="));

        assertThat(stagingRecipient.toString()).isEqualTo("BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=");
    }

    @Test
    public void testEquals() {
        StagingRecipient stagingRecipient =
                new StagingRecipient(Base64.getDecoder().decode("BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo="));
        stagingRecipient.setId(1L);

        StagingRecipient stagingRecipient2 = new StagingRecipient();
        stagingRecipient2.setId(1L);
        stagingRecipient2.setRecBytes("BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=");

        StagingRecipient stagingRecipient3 =
                new StagingRecipient(Base64.getDecoder().decode("BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey1Ao="));
        stagingRecipient3.setId(3L);

        assertThat(stagingRecipient).isNotEqualTo(new Object());

        assertThat(stagingRecipient).isNotEqualTo(stagingRecipient3);
        assertThat(stagingRecipient).isEqualTo(stagingRecipient2);

        assertThat(stagingRecipient.getRecBytes())
            .isEqualTo(stagingRecipient2.getRecBytes());
    }
}
