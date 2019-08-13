
package com.quorum.tessera.transaction.exception;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;


public class NoRecipientKeyFoundExceptionTest {
    
    @Test
    public void create() {
        NoRecipientKeyFoundException ex = new NoRecipientKeyFoundException("HEllow");
        assertThat(ex).hasNoCause();
        assertThat(ex).hasMessage("HEllow");
    }
}
