package com.quorum.tessera.config;

import javax.xml.bind.JAXBElement;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;

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
        Config configuration = mock(Config.class);
        JAXBElement<Config> element
                = objectFactory.createConfiguration(configuration);

        assertThat(element).isNotNull();
        assertThat(element.getValue()).isSameAs(configuration);

    }

   

}
