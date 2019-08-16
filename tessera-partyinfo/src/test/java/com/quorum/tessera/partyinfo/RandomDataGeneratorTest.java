
package com.quorum.tessera.partyinfo;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;


public class RandomDataGeneratorTest {
    
    @Test
    public void generate() {
        assertThat(new RandomDataGenerator() {}.generate())
                .isNotNull()
                .isNotBlank();
    }
    
}
