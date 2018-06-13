package com.github.nexus.configuration;

import org.junit.Test;

import java.util.Collections;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ConfigurationParserTest {

    @Test
    public void propertiesGetLoaded() {
        final ConfigurationParser parser = new ConfigurationParserImpl();
        final PropertyLoader loader = mock(PropertyLoader.class);

        final Properties returnedProperties = new Properties();
        returnedProperties.put("url", "test-url.com");
        doReturn(returnedProperties).when(loader).getAllProperties(any());

        final Configuration config = parser.config(loader, Collections.emptyList());

        assertThat(config.url()).isEqualTo("test-url.com");
    }

    @Test
    public void serviceLoaderCreatesInstance() {

        final ConfigurationParser configurationParser = ConfigurationParser.create();

        assertThat(configurationParser).isInstanceOf(ConfigurationParserImpl.class);

    }

}
