package com.quorum.tessera.config.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.quorum.tessera.key.generation.KeyGeneratorFactory;
import org.junit.Before;
import org.junit.Test;

public class KeyGenCommandFactoryTest {

  private KeyGenCommandFactory keyGenCommandFactory;

  @Before
  public void beforeTest() {
    keyGenCommandFactory = new KeyGenCommandFactory();
  }

  @Test
  public void createNonKeyGenCommandThrows() throws Exception {
    TesseraCommand command = keyGenCommandFactory.create(TesseraCommand.class);
    assertThat(command).isNotNull();
  }

  @Test
  public void create() throws Exception {

    try (var staticKeyGeneratorFactory = mockStatic(KeyGeneratorFactory.class);
        var staticKeyDataMarshaller = mockStatic(KeyDataMarshaller.class)) {

      staticKeyGeneratorFactory
          .when(KeyGeneratorFactory::create)
          .thenReturn(mock(KeyGeneratorFactory.class));
      staticKeyDataMarshaller
          .when(KeyDataMarshaller::create)
          .thenReturn(mock(KeyDataMarshaller.class));

      KeyGenCommand command = keyGenCommandFactory.create(KeyGenCommand.class);

      assertThat(command).isNotNull();

      staticKeyGeneratorFactory.verify(KeyGeneratorFactory::create);
      staticKeyGeneratorFactory.verifyNoMoreInteractions();

      staticKeyDataMarshaller.verify(KeyDataMarshaller::create);
      staticKeyDataMarshaller.verifyNoMoreInteractions();
    }
  }
}
