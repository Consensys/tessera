package com.github.nexus.configuration;

import org.junit.Test;

import java.nio.file.Paths;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigImplTest {

    @Test
    public void gettersManipulateProperties() {

        final Properties configProperties = new Properties();

        configProperties.setProperty("keygenBasePath", "basepath");
        configProperties.setProperty("publicKeys", "key1,key2,key3");
        configProperties.setProperty("privateKeys", "priv1,priv2");
        configProperties.setProperty("url", "http://url.com");
        configProperties.setProperty("port", "2000");
        configProperties.setProperty("othernodes", "node1.com,node2.com:10000");

        final Configuration configuration = new ConfigurationImpl(configProperties);

        assertThat(configuration.keygenBasePath()).isEqualTo(Paths.get("basepath").toAbsolutePath());
        assertThat(configuration.publicKeys()).hasSize(3).containsExactly("key1", "key2", "key3");
        assertThat(configuration.privateKeys()).isEqualTo("priv1,priv2");
        assertThat(configuration.url()).isEqualTo("http://url.com");
        assertThat(configuration.port()).isEqualTo(2000);
        assertThat(configuration.othernodes()).hasSize(2).containsExactly("node1.com", "node2.com:10000");

    }


}
