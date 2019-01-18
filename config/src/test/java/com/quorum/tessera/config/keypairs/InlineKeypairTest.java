package com.quorum.tessera.config.keypairs;

import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.PrivateKeyData;
import com.quorum.tessera.config.PrivateKeyType;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InlineKeypairTest {

    @Test
    public void missingPasswordGetsCorrectFailureToken() {

        //even though its an unlocked key, we have specified it as locked
        //so a locked key + missing password is picked up

        final KeyDataConfig privKeyDataConfig = new KeyDataConfig(
            new PrivateKeyData("Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=", null, null, null, null),
            PrivateKeyType.LOCKED
        );

        final InlineKeypair result = new InlineKeypair("public", privKeyDataConfig);

        assertThat(result.getPrivateKey()).isEqualTo("MISSING_PASSWORD");
    }

}
