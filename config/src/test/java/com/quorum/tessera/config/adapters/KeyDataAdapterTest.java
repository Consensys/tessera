package com.quorum.tessera.config.adapters;

import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.PrivateKeyData;
import com.quorum.tessera.config.PrivateKeyType;
import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class KeyDataAdapterTest {

    private KeyDataAdapter adapter = new KeyDataAdapter();

    @Test
    public void marshallUnlockedKey() {

        final KeyData keyData = new KeyData(new KeyDataConfig(null, PrivateKeyType.UNLOCKED), "PRIV", "PUB");

        final KeyData marshalledKey = adapter.marshal(keyData);

        assertThat(marshalledKey.getPrivateKey()).isEqualTo("PRIV");
        assertThat(marshalledKey.getPublicKey()).isEqualTo("PUB");
        assertThat(marshalledKey.getConfig()).isEqualToComparingFieldByField(new KeyDataConfig(null, PrivateKeyType.UNLOCKED));

    }

    @Test
    public void marshallLockedKeyNullifiesPrivateKey() {

        final KeyData keyData = new KeyData(new KeyDataConfig(null, PrivateKeyType.LOCKED), "PRIV", "PUB");

        final KeyData marshalledKey = adapter.marshal(keyData);

        assertThat(marshalledKey.getConfig()).isEqualToComparingFieldByField(keyData.getConfig());
        assertThat(marshalledKey.getPublicKey()).isEqualTo("PUB");
        assertThat(marshalledKey.getPrivateKey()).isNull();

    }

    @Test
    public void marshallKeysWithLiteralValues() {

        final KeyData keyData = new KeyData(null, "PRIV", "PUB");

        final KeyData marshalled = adapter.unmarshal(keyData);

        assertThat(marshalled).isEqualToComparingFieldByField(keyData);

    }

    @Test
    public void marshallKeysWithUnlockedPrivateKey() {

        final KeyData keyData = new KeyData(
            new KeyDataConfig(new PrivateKeyData("LITERAL_PRIVATE", null, null, null, null, null), PrivateKeyType.UNLOCKED),
            null,
            "PUB"
        );

        final KeyData marshalled = adapter.unmarshal(keyData);

        assertThat(marshalled.getPrivateKey()).isEqualTo("LITERAL_PRIVATE");

    }

    @Test
    public void marshallKeysWithLockedPrivateKey() {

        final KeyData keyData = new KeyData(
            new KeyDataConfig(
                new PrivateKeyData(
                    null,
                    "x3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC",
                    "7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=",
                    "d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc",
                    new ArgonOptions("id", 10, 1048576, 4),
                    "q"
                ),
                PrivateKeyType.LOCKED
            ),
            null,
            "PUB"
        );

        final KeyData marshalled = adapter.unmarshal(keyData);

        assertThat(marshalled.getPrivateKey()).isEqualTo("6ccai0+GXRRVbNckE+JubN+UQ9+8pMCx86dZI683X7w=");

    }

}
