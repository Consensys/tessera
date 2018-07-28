package com.quorum.tessera.config.adapters;

import com.quorum.tessera.nacl.Key;
import org.junit.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyAdapterTest {

    private final KeyAdapter keyAdapter = new KeyAdapter();

    @Test
    public void keyIsMarshalledToBase64() {

        final byte[] keyBytes = new byte[]{1, 2, 3, 4, 5, 6};

        final Key key = new Key(keyBytes);

        final String marshalled = this.keyAdapter.marshal(key);

        assertThat(marshalled).isEqualTo(Base64.getEncoder().encodeToString(keyBytes));

    }

    @Test
    public void unmarshalledToKey() {

        final String base64Key = "yAWAJjwPqUtNVlqGjSrBmr1/iIkghuOh1803Yzx9jLM=";

        final Key unmarshalled = this.keyAdapter.unmarshal(base64Key);

        assertThat(unmarshalled.toString()).isEqualTo(base64Key);

    }

}
