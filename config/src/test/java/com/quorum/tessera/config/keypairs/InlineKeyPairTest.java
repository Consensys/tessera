package com.quorum.tessera.config.keypairs;

import com.quorum.tessera.config.KeyDataConfig;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class InlineKeyPairTest {

    @Test
    public void getTypeReturnsInline() {
        KeyDataConfig keyConfig = mock(KeyDataConfig.class);
        InlineKeypair keyPair = new InlineKeypair("public", keyConfig);
        assertThat(keyPair.getType()).isEqualByComparingTo(ConfigKeyPairType.INLINE);
    }

}
