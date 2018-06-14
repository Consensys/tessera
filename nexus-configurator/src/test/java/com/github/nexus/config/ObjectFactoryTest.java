package com.github.nexus.config;

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
        objectFactory = null;
    }
    
    @Test
    public void testCreateConfiguration() {
        assertThat(objectFactory.createConfiguration()).isNotNull();
    }
    
    @Test
    public void testCreateConfigurationJAXBElement() {
        Configuration config = new Configuration();
        JAXBElement<Configuration> result = objectFactory.createConfiguration(config);
        assertThat(result).isNotNull();
        assertThat(result.getValue()).isSameAs(config);
    }  
    
}
