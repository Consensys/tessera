package com.github.nexus.config.jaxb;

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

        JAXBElement<Configuration> element
                = objectFactory.createConfiguration((Configuration) configuration);

        assertThat(element).isNotNull();
        assertThat(element.getValue()).isSameAs(configuration);

    }

    @Test
    public void createJdbcConfg() {
        JdbcConfig jdbcConfig = objectFactory.createJdbcConfig();
        assertThat(jdbcConfig).isNotNull();

    }

    @Test
    public void createPeer() {
        Peer peer = objectFactory.createPeer();
        assertThat(peer).isNotNull();
    }

    @Test
    public void createServerConfig() {
        ServerConfig serverConfig = objectFactory.createServerConfig();
        assertThat(serverConfig).isNotNull();
    }

    @Test
    public void createPrivateKey() {
        PrivateKey privateKey = objectFactory.createPrivateKey();
        assertThat(privateKey).isNotNull();
    }

    @Test
    public void createPublicKey() {
        PublicKey publicKey = objectFactory.createPublicKey();
        assertThat(publicKey).isNotNull();
    }

}
