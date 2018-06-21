
package com.github.nexus.config.api;

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
        Configuration configuration = objectFactory.createConfiguration();
        assertThat(configuration).isNotNull();
        
        JAXBElement<Configuration> element = objectFactory.createConfiguration(configuration);
        
        assertThat(element).isNotNull();
        assertThat(element.getValue()).isSameAs(configuration);
        
    }
    
}
