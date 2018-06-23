package com.github.nexus.config;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class JaxbConfigFactoryTest {

    private JaxbConfigFactory jaxbConfigFactory;

    public JaxbConfigFactoryTest() {
    }

    @Before
    public void setUp() throws SAXException {
        jaxbConfigFactory = new JaxbConfigFactory();
    }

    @After
    public void tearDown() {
        jaxbConfigFactory = null;
    }

    @Test
    public void createFromXml() throws Exception {

        try (InputStream inputStream = getClass().getResourceAsStream("/sample.xml")) {
            Config config = jaxbConfigFactory.create(inputStream);

            assertThat(config).isNotNull();
            assertThat(config.getJdbcConfig().getUsername()).isEqualTo("scott");

        }
    }

    @Test(expected = ConfigException.class)
    public void createInvalid() throws Exception {

        try (InputStream inputStream = new ByteArrayInputStream("<foo />".getBytes())) {
            jaxbConfigFactory.create(inputStream);

        }
    }

}
