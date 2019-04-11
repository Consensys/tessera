package com.quorum.tessera.reflect;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class ReflectCallbackTest {

    @Test(expected = ReflectException.class)
    public void executeThrowsClassNotFoundException() throws Exception {
        ReflectCallback callback = mock(ReflectCallback.class);

        doThrow(ClassNotFoundException.class).when(callback).doExecute();

        ReflectCallback.execute(callback);
    }

    @Test
    public void execute() {
        ReflectCallback<String> callback = () -> "Expected value";

        String result = ReflectCallback.execute(callback);
        assertThat(result).isEqualTo("Expected value");
    }
}
