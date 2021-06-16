package com.quorum.tessera.context.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.mock;

import com.quorum.tessera.context.RuntimeContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RuntimeContextHolderTest {

  @After
  @Before
  public void clearHolder() {
    RuntimeContextHolder.INSTANCE.setContext(null);
  }

  @Test
  public void setContextCanOnlyBeStoredOnce() {

    RuntimeContextHolder contextHolder = RuntimeContextHolder.INSTANCE;
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
}
