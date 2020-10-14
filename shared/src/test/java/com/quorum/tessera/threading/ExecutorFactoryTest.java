package com.quorum.tessera.threading;

import org.junit.Test;

import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;

public class ExecutorFactoryTest {

    @Test
    public void createCachedThreadPool() {
        Executor executor = new ExecutorFactory().createCachedThreadPool();
        assertThat(executor).isNotNull();
    }
}
