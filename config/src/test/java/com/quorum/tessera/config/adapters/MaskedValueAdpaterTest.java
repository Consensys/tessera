package com.quorum.tessera.config.adapters;

import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

public class MaskedValueAdpaterTest {

    private MaskedValueAdpater maskedValueAdpater = new MaskedValueAdpater();

    @Test
    public void unmarshalDoesNothin() throws Exception {
        assertThat(maskedValueAdpater.unmarshal("HELLOW")).isEqualTo("HELLOW");
    }

    @Test
    public void unmarshalNUllDoesNothin() throws Exception {
        assertThat(maskedValueAdpater.unmarshal(null)).isNull();
    }
    
    @Test
    public void marshallMasksValue() throws Exception {
        assertThat(maskedValueAdpater.marshal("HELLOW")).isEqualTo("******");
    }
    
        @Test
    public void marshallNullReturnsNull() throws Exception {
        assertThat(maskedValueAdpater.marshal(null)).isNull();
    }

}
