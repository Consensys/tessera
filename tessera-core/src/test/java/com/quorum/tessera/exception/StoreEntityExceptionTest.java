
package com.quorum.tessera.exception;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class StoreEntityExceptionTest {
    
    @Test
    public void create() {
        Exception cause = new Exception("Ouch");
        StoreEntityException ex = new StoreEntityException("HEllow", cause);
        assertThat(ex).hasCause(cause);
        assertThat(ex).hasMessage("HEllow");
    }
    
}
