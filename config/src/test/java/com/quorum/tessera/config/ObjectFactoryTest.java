package com.quorum.tessera.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import jakarta.xml.bind.JAXBElement;
import org.junit.Before;
import org.junit.Test;

public class ObjectFactoryTest {

  private ObjectFactory objectFactory;

  @Before
  public void setUp() {
    this.objectFactory = new ObjectFactory();
  }

  @Test
  public void createConfiguration() {
    final Config configuration = mock(Config.class);
    final JAXBElement<Config> element = objectFactory.createConfiguration(configuration);

    assertThat(element).isNotNull();
    assertThat(element.getValue()).isSameAs(configuration);
  }
}
