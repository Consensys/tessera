package com.quorum.tessera.config.keypairs;

import com.quorum.tessera.config.KeyDataConfig;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class InlineKeypairTest {

    @Test
    public void getType() {
        InlineKeypair keyPair = new InlineKeypair("pub", mock(KeyDataConfig.class));
        assertThat(keyPair.getType()).isEqualTo(KeyPairType.INLINE);
    }
}
