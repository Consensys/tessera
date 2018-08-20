package com.quorum.tessera.config.keys;

import com.quorum.tessera.nacl.Key;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class KeysConverterTest {


    @Test
    public void convertKeysSingleValueConvertorDecodesValuesToBase64() {

        String sample = "HELLOW";
        
        byte[] value = Base64.getDecoder().decode(sample);
        
        List<String> values = Arrays.asList(sample);

        List<Key> results = KeysConverter.convert(values);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getKeyBytes()).isEqualTo(value);

    }


}
