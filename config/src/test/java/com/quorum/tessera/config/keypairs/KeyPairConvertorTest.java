package com.quorum.tessera.config.keypairs;

import com.quorum.tessera.encryption.KeyPair;
import java.util.Arrays;
import java.util.Collection;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class KeyPairConvertorTest {

    @Test
    public void convert() {
        final DirectKeyPair configKeyPair = new DirectKeyPair("public", "private");
        Collection<KeyPair> result = KeyPairConvertor.convert(Arrays.asList(configKeyPair));
        assertThat(result).hasSize(1);
        

    }

}
