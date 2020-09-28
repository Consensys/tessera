package com.quorum.tessera.threading;

import org.junit.Test;

import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;

public class ExecutorFactoryTest {

    @Test
    public void create() {
        Executor executor = new ExecutorFactory().create();
        assertThat(executor).isNotNull();
    }
}
