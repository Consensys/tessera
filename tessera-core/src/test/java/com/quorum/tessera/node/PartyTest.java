
package com.quorum.tessera.node;

import com.quorum.tessera.node.model.Party;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;


public class PartyTest {
    
    @Test
    public void toStringContainsUrl() {
        Party party = new Party("someurl");
        
        assertThat(party.toString()).contains("someurl");

    }
    
    
}
