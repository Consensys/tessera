package com.quorum.tessera.api.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SendRequestTest {

    @Test
    public void getRecipientsWhenNull() {

        final SendRequest sendRequest = new SendRequest();
        sendRequest.setTo(null);

        final String[] recipients = sendRequest.getTo();

        assertThat(recipients).isNotNull().hasSize(0);

    }

}
