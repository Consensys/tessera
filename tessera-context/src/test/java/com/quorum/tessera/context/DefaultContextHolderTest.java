package com.quorum.tessera.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.mock;

import org.junit.Test;

public class DefaultContextHolderTest extends ContextTestCase {

  private DefaultContextHolder contextHolder = DefaultContextHolder.INSTANCE;

  @Test
  public void setContextCanOnlyBeStoredOnce() {

    RuntimeContext runtimeContext = mock(RuntimeContext.class);
    contextHolder.setContext(runtimeContext);

    assertThat(contextHolder.getContext().get()).isSameAs(runtimeContext);

    try {
      contextHolder.setContext(mock(RuntimeContext.class));
      failBecauseExceptionWasNotThrown(IllegalStateException.class);
    } catch (IllegalStateException ex) {
      assertThat(ex).hasMessage("RuntimeContext has already been stored");
    }
  }

  @Test
  public void getContextIfNotPresent() {
    assertThat(contextHolder.getContext()).isNotPresent();
  }
}
