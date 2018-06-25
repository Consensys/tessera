
package com.github.nexus.config.api;

import com.github.nexus.config.Config;
import javax.xml.bind.JAXBElement;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class ObjectFactoryTest {
    
    private ObjectFactory objectFactory;
    
    public ObjectFactoryTest() {
    }
    
    @Before
    public void setUp() {
         objectFactory = new ObjectFactory();
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void createConfiguration() {
        Config configuration = objectFactory.createConfiguration();
        assertThat(configuration).isNotNull();
        
        JAXBElement<Configuration> element = 
                objectFactory.createConfiguration((Configuration)configuration);
        
        assertThat(element).isNotNull();
        assertThat(element.getValue()).isSameAs(configuration);
        
    }
    
}
