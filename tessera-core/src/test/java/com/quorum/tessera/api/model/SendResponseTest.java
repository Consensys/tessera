package com.quorum.tessera.api.model;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class SendResponseTest {

    @Test
    public void createInstanceWithKey() {
        String key = "HELLOW";
        SendResponse instance = new SendResponse(key);

        assertThat(instance.getKey()).isEqualTo(key);
    }
}
