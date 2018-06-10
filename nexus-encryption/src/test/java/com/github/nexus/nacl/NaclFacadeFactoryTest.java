
package com.github.nexus.nacl;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;


public class NaclFacadeFactoryTest {
    
    
    private NaclFacadeFactory facadeFactory;
    
    public NaclFacadeFactoryTest() {
    }
    
    @Before
    public void onSetUp() {
      facadeFactory=  NaclFacadeFactory.newFactory();
      assertThat(facadeFactory)
              .isExactlyInstanceOf(MockNaclFacadeFactory.class);
    }
    
    @Test
    public void create() {
        NaclFacade result = facadeFactory.create();
        assertThat(result).isNotNull()
                .isSameAs(MockNaclFacade.INSTANCE);
        
                
        
    }

    
}
