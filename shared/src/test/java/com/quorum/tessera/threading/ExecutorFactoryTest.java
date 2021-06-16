package com.quorum.tessera.threading;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.Executor;
import org.junit.Test;

public class ExecutorFactoryTest {

  @Test
  public void createCachedThreadPool() {
    Executor executor = new ExecutorFactory().createCachedThreadPool();
    assertThat(executor).isNotNull();
  }
}
