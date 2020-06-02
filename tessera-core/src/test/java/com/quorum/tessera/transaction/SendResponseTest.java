package com.quorum.tessera.transaction;

import com.quorum.tessera.data.MessageHash;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SendResponseTest {

    @Test
    public void fromTransactionHash() {
        MessageHash transactionHash = mock(MessageHash.class);
        SendResponse response = SendResponse.from(transactionHash);

        assertThat(response).isNotNull();
        assertThat(response.getTransactionHash()).isSameAs(transactionHash);

    }

    @Test(expected = NullPointerException.class)
    public void buildWithNothing() {

        SendResponse.from(null);

    }
}
