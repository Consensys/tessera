package com.quorum.tessera.config.cli;


import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keypairs.DirectKeyPair;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyDataMarshallerTest {

    @Test
    public void create() {
        KeyDataMarshaller keyDataMarshaller = KeyDataMarshaller.create();
        assertThat(keyDataMarshaller).isExactlyInstanceOf(MockKeyDataMarshaller.class);
    }

    @Test
    public void defaultMarshal() {
        KeyDataMarshaller k = new KeyDataMarshaller() {};

        DirectKeyPair configKeyPair = new DirectKeyPair("PUBLIC","PRIVATE");
        KeyData keyData = k.marshal(configKeyPair);
        assertThat(keyData).isNotNull();
        assertThat(keyData.getPublicKey()).isEqualTo("PUBLIC");
        assertThat(keyData.getPrivateKey()).isEqualTo("PRIVATE");
    }

}
