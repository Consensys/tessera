package com.quorum.tessera.data;


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
        StagingRecipient stagingRecipient = new StagingRecipient(
            Base64.getDecoder().decode("BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=")
        );
        StagingRecipient stagingRecipient2 = new StagingRecipient();
        stagingRecipient2.setRecBytes("BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=");

        StagingRecipient stagingRecipient3 = new StagingRecipient(
            Base64.getDecoder().decode("BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey1Ao=")
        );

        assertThat(stagingRecipient.equals(new Object())).isFalse();
        assertThat(stagingRecipient.equals(stagingRecipient3)).isFalse();
        assertThat(stagingRecipient.equals(stagingRecipient2)).isTrue();
        assertThat(stagingRecipient.getRecBytes()).isEqualTo(stagingRecipient2.getRecBytes());
    }
}
