package com.github.nexus.nacl.kalium;

import com.github.nexus.nacl.NaclFacade;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;

public class KaliumFactoryTest {
    
    private KaliumFactory kaliumFactory;
    
    public KaliumFactoryTest() {
    }
    
    @Before
    public void setUp() {
        kaliumFactory = new KaliumFactory();
    }
    
    @Test
    public void createInstance() {
        NaclFacade result =  kaliumFactory.create();
        assertThat(result).isNotNull().isExactlyInstanceOf(Kalium.class);
    }
    
}
