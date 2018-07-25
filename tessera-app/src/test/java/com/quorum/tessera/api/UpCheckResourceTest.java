
package com.quorum.tessera.api;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;


public class UpCheckResourceTest  {
    
    private UpCheckResource resource;
    
    public UpCheckResourceTest() {
    }
    
    @Before
    public void onSetUp() {
        resource = new UpCheckResource();
    }

    @Test
    public void upcheck() {

        assertThat(resource.upCheck())
                .isEqualTo("I'm up!");

    }
}
